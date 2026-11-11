package bg.softuni.tradezone.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

  private final SecretKey key;
  private final Duration ttl;

  public JwtProvider(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.expiration}") long expirationSeconds) {
    // throws WeakKeyException on startup if the secret is shorter than 256 bits
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.ttl = Duration.ofSeconds(expirationSeconds);
  }

  public static String bearer(String header) {
    return header != null && header.startsWith("Bearer ") ? header.substring(7) : null;
  }

  public String generate(String username) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(username)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(ttl)))
        .signWith(key)
        .compact();
  }

  /**
   * @return the username, or null when the token is malformed, forged or expired
   */
  public String username(String token) {
    try {
      return Jwts.parser()
          .verifyWith(key)
          .build()
          .parseSignedClaims(token)
          .getPayload()
          .getSubject();
    } catch (JwtException | IllegalArgumentException e) {
      return null;
    }
  }
}
