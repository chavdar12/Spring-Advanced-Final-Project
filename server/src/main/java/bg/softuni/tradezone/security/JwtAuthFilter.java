package bg.softuni.tradezone.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Authenticates {@code Authorization: Bearer <jwt>}; anything else stays anonymous and is rejected
 * by the rules.
 */
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtProvider jwt;
  private final UserDetailsService users;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String token = JwtProvider.bearer(request.getHeader(HttpHeaders.AUTHORIZATION));
    String username = token == null ? null : jwt.username(token);
    if (username != null) {
      try {
        UserDetails user = users.loadUserByUsername(username);
        SecurityContextHolder.getContext()
            .setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                    user, null, user.getAuthorities()));
      } catch (UsernameNotFoundException ignored) {
        // token of a deleted user: stays anonymous
      }
    }
    chain.doFilter(request, response);
  }
}
