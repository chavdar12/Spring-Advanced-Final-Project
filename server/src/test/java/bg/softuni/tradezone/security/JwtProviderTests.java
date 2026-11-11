package bg.softuni.tradezone.security;

import static org.junit.jupiter.api.Assertions.*;

import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.Test;

class JwtProviderTests {

  private static final String SECRET =
      "a-test-secret-that-is-long-enough-for-hs512-signing-0123456789abcdef";

  private final JwtProvider provider = new JwtProvider(SECRET, 60);

  @Test
  void generatedToken_carriesUsername() {
    assertEquals("pesho", provider.username(provider.generate("pesho")));
  }

  @Test
  void tamperedToken_isRejected() {
    String token = provider.generate("pesho");
    assertNull(
        provider.username(
            token.substring(0, token.length() - 2) + (token.endsWith("AA") ? "BB" : "AA")));
  }

  @Test
  void tokenSignedWithAnotherKey_isRejected() {
    assertNull(provider.username(new JwtProvider(SECRET.replace('a', 'b'), 60).generate("pesho")));
  }

  @Test
  void expiredToken_isRejected() {
    JwtProvider expiring = new JwtProvider(SECRET, -1);
    assertNull(expiring.username(expiring.generate("pesho")));
  }

  @Test
  void garbageAndEmptyTokens_areRejected() {
    assertNull(provider.username("not.a.jwt"));
    assertNull(provider.username(""));
  }

  @Test
  void bearerHeader_isParsed() {
    assertEquals("abc", JwtProvider.bearer("Bearer abc"));
    assertNull(JwtProvider.bearer("Basic abc"));
    assertNull(JwtProvider.bearer(null));
  }

  @Test
  void shortSecret_isRejectedAtStartup() {
    assertThrows(WeakKeyException.class, () -> new JwtProvider("jwtWW3SecretKey", 60));
  }
}
