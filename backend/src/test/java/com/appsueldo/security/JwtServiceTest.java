package com.appsueldo.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.appsueldo.entity.User;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

class JwtServiceTest {

    @Test
    void createAccessTokenSignsHs256TokenThatCanBeDecoded() {
        SecretKey key = hmacKey("test-secret-with-at-least-32-characters");
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key.getEncoded()));
        JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
            .macAlgorithm(MacAlgorithm.HS256)
            .build();
        JwtService jwtService = new JwtService(encoder, 900000);

        User user = new User();
        setId(user, 123L);
        user.setEmail("test@appsueldo.local");
        user.setName("Test User");

        String token = jwtService.createAccessToken(user);
        Jwt decoded = decoder.decode(token);

        assertThat(decoded.getSubject()).isEqualTo("123");
        assertThat(decoded.getClaimAsString("email")).isEqualTo("test@appsueldo.local");
        assertThat(decoded.getClaimAsString("name")).isEqualTo("Test User");
        assertThat(decoded.getClaimAsString("scope")).isEqualTo("USER");
    }

    private SecretKey hmacKey(String secret) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(secretBytes, "HmacSHA256");
    }

    private void setId(User user, Long id) {
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not set user id for test.", exception);
        }
    }
}
