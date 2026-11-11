package bg.softuni.tradezone.config.security.jwt;

import bg.softuni.tradezone.model.entity.User;
import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.*;

class JwtProviderTests {

    private static final String SECRET = "a-test-secret-that-is-long-enough-for-hs512-signing-0123456789abcdef";

    private final JwtProvider provider = new JwtProvider(SECRET, 60);

    private String tokenFor(String username) {
        User user = new User(username, username + "@mail.bg", "pass");
        return provider.generateJwtToken(new UsernamePasswordAuthenticationToken(user, null));
    }

    @Test
    void generatedToken_isValid_andCarriesUsername() {
        String token = tokenFor("pesho");

        assertTrue(provider.validateJwtToken(token));
        assertEquals("pesho", provider.getUserNameFromJwtToken(token));
    }

    @Test
    void tamperedToken_isInvalid() {
        String token = tokenFor("pesho");
        String tampered = token.substring(0, token.length() - 2) + (token.endsWith("AA") ? "BB" : "AA");

        assertFalse(provider.validateJwtToken(tampered));
    }

    @Test
    void tokenSignedWithAnotherKey_isInvalid() {
        JwtProvider other = new JwtProvider(SECRET.replace('a', 'b'), 60);
        User user = new User("pesho", "pesho@mail.bg", "pass");
        String foreign = other.generateJwtToken(new UsernamePasswordAuthenticationToken(user, null));

        assertFalse(provider.validateJwtToken(foreign));
    }

    @Test
    void expiredToken_isInvalid() {
        JwtProvider expiring = new JwtProvider(SECRET, -1);
        User user = new User("pesho", "pesho@mail.bg", "pass");

        assertFalse(expiring.validateJwtToken(expiring.generateJwtToken(new UsernamePasswordAuthenticationToken(user, null))));
    }

    @Test
    void garbageAndEmptyTokens_areInvalid() {
        assertFalse(provider.validateJwtToken("not.a.jwt"));
        assertFalse(provider.validateJwtToken(""));
    }

    @Test
    void shortSecret_isRejectedAtStartup() {
        assertThrows(WeakKeyException.class, () -> new JwtProvider("jwtWW3SecretKey", 60));
    }
}
