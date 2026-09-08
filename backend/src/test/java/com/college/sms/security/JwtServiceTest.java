package com.college.sms.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-test-secret-1234";

    private final JwtService jwtService = new JwtService(SECRET, 60_000);

    @Test
    void generatesAndParsesToken() {
        String token = jwtService.generateToken(1L, "admin", "SUPER_ADMIN");

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).contains("admin");
    }

    @Test
    void tokenCarriesUidAndRoleClaims() {
        String token = jwtService.generateToken(42L, "student", "STUDENT");

        var claims = jwtService.parse(token).orElseThrow();

        assertThat(claims.get("uid", Long.class)).isEqualTo(42L);
        assertThat(claims.get("role", String.class)).isEqualTo("STUDENT");
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = jwtService.generateToken(1L, "admin", "SUPER_ADMIN");
        // flip a character inside the signature segment to corrupt it
        String corrupted = token.substring(0, token.length() - 3)
                + (token.endsWith("AAA") ? "BBB" : "AAA");

        assertThat(jwtService.parse(corrupted)).isEmpty();
    }

    @Test
    void tokenSignedWithDifferentSecretIsRejected() {
        String token = new JwtService("other-secret-other-secret-other-secret-other-secret", 60_000)
                .generateToken(1L, "admin", "SUPER_ADMIN");

        assertThat(jwtService.parse(token)).isEmpty();
    }

    @Test
    void expiredTokenIsRejected() {
        JwtService expiring = new JwtService(SECRET, -1000);
        String token = expiring.generateToken(1L, "admin", "SUPER_ADMIN");

        assertThat(jwtService.parse(token)).isEmpty();
    }
}
