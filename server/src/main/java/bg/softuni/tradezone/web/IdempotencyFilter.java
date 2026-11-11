package bg.softuni.tradezone.web;

import bg.softuni.tradezone.error.ProblemWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Base64;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * Makes unsafe requests carrying an {@code Idempotency-Key} header safe to retry: the first
 * response is stored in Redis and replayed for repeats of the same key + payload. Runs after Spring
 * Security, so keys are scoped per authenticated user.
 */
@Component
public class IdempotencyFilter extends OncePerRequestFilter {

  public static final String HEADER = "Idempotency-Key";
  static final String REPLAYED_HEADER = "Idempotent-Replayed";
  private static final Set<String> METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
  private static final String IN_PROGRESS = "0";
  private static final Duration LOCK_TTL = Duration.ofMinutes(1);

  private final StringRedisTemplate redis;
  private final ProblemWriter problems;
  private final Duration ttl;

  public IdempotencyFilter(
      StringRedisTemplate redis,
      ProblemWriter problems,
      @Value("${app.idempotency.ttl:24h}") Duration ttl) {
    this.redis = redis;
    this.problems = problems;
    this.ttl = ttl;
  }

  private static String principal() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return auth == null || !auth.isAuthenticated() ? "anonymous" : auth.getName();
  }

  private void replay(
      String stored, String fingerprint, HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    if (stored == null) { // expired between SETNX and GET
      problems.write(
          request,
          response,
          HttpStatus.CONFLICT,
          "A request with this Idempotency-Key is being retried");
      return;
    }
    String[] parts = stored.split("\\|", 4);
    if (!parts[1].equals(fingerprint)) {
      problems.write(
          request,
          response,
          HttpStatus.UNPROCESSABLE_CONTENT,
          "This Idempotency-Key was already used with a different payload");
      return;
    }
    if (parts[0].equals(IN_PROGRESS)) {
      problems.write(
          request,
          response,
          HttpStatus.CONFLICT,
          "A request with this Idempotency-Key is still in progress");
      return;
    }
    response.setStatus(Integer.parseInt(parts[0]));
    if (!"null".equals(parts[2])) {
      response.setContentType(parts[2]);
    }
    response.setHeader(REPLAYED_HEADER, "true");
    response.getOutputStream().write(Base64.getDecoder().decode(parts[3]));
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String contentType = request.getContentType();
    // shortcut: multipart bodies are not fingerprinted, so they bypass idempotency; add if uploads
    // need retries
    return request.getHeader(HEADER) == null
        || !METHODS.contains(request.getMethod())
        || (contentType != null && contentType.startsWith(MediaType.MULTIPART_FORM_DATA_VALUE));
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String key = request.getHeader(HEADER);
    if (key.isBlank() || key.length() > 255) {
      problems.write(
          request, response, HttpStatus.BAD_REQUEST, "Idempotency-Key must be 1-255 characters");
      return;
    }

    byte[] body = request.getInputStream().readAllBytes();
    String fingerprint = DigestUtils.md5DigestAsHex(body);
    String redisKey =
        "idempotency:%s:%s %s:%s"
            .formatted(principal(), request.getMethod(), request.getRequestURI(), key);

    Boolean acquired =
        redis.opsForValue().setIfAbsent(redisKey, IN_PROGRESS + "|" + fingerprint, LOCK_TTL);
    if (!Boolean.TRUE.equals(acquired)) {
      replay(redis.opsForValue().get(redisKey), fingerprint, request, response);
      return;
    }

    ContentCachingResponseWrapper cached = new ContentCachingResponseWrapper(response);
    try {
      chain.doFilter(new CachedBodyRequest(request, body), cached);
    } catch (IOException | ServletException | RuntimeException e) {
      redis.delete(redisKey);
      throw e;
    }

    if (cached.getStatus() >= 500) {
      redis.delete(redisKey); // server failures stay retryable
    } else {
      String stored =
          String.join(
              "|",
              String.valueOf(cached.getStatus()),
              fingerprint,
              String.valueOf(cached.getContentType()),
              Base64.getEncoder().encodeToString(cached.getContentAsByteArray()));
      redis.opsForValue().set(redisKey, stored, ttl);
    }
    cached.copyBodyToResponse();
  }

  private static final class CachedBodyRequest extends HttpServletRequestWrapper {
    private final byte[] body;

    CachedBodyRequest(HttpServletRequest request, byte[] body) {
      super(request);
      this.body = body;
    }

    @Override
    public ServletInputStream getInputStream() {
      ByteArrayInputStream in = new ByteArrayInputStream(body);
      return new ServletInputStream() {
        @Override
        public int read() {
          return in.read();
        }

        @Override
        public int read(byte[] b, int off, int len) {
          return in.read(b, off, len);
        }

        @Override
        public boolean isFinished() {
          return in.available() == 0;
        }

        @Override
        public boolean isReady() {
          return true;
        }

        @Override
        public void setReadListener(ReadListener listener) {
          throw new UnsupportedOperationException();
        }
      };
    }
  }
}
