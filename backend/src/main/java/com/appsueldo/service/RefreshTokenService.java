package com.appsueldo.service;

import com.appsueldo.entity.RefreshToken;
import com.appsueldo.entity.User;
import com.appsueldo.exception.InvalidRefreshTokenException;
import com.appsueldo.repository.RefreshTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    private final long expirationMillis;

    public RefreshTokenService(
        RefreshTokenRepository refreshTokenRepository,
        @Value("${app.refresh-token.expiration}") long expirationMillis
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.expirationMillis = expirationMillis;
    }

    @Transactional
    public CreatedRefreshToken create(User user) {
        String rawToken = generateRawToken();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(hash(rawToken));
        refreshToken.setExpiresAt(Instant.now().plusMillis(expirationMillis));
        refreshToken.setRevoked(false);
        return new CreatedRefreshToken(rawToken, refreshTokenRepository.save(refreshToken));
    }

    @Transactional
    public RotatedRefreshToken rotate(String rawToken) {
        RefreshToken current = findValid(rawToken);
        current.setRevoked(true);
        CreatedRefreshToken next = create(current.getUser());
        return new RotatedRefreshToken(current.getUser(), next.rawToken(), next.entity());
    }

    @Transactional
    public void revoke(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHashAndRevokedFalse(hash(rawToken))
            .ifPresent(token -> token.setRevoked(true));
    }

    @Transactional
    public void revokeAll(User user) {
        refreshTokenRepository.findByUserAndRevokedFalse(user)
            .forEach(token -> token.setRevoked(true));
    }

    public String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 no esta disponible.", exception);
        }
    }

    private RefreshToken findValid(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidRefreshTokenException("Refresh token ausente.");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHashAndRevokedFalse(hash(rawToken))
            .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token invalido."));

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            refreshToken.setRevoked(true);
            throw new InvalidRefreshTokenException("Refresh token expirado.");
        }

        return refreshToken;
    }

    private String generateRawToken() {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public record CreatedRefreshToken(String rawToken, RefreshToken entity) {
    }

    public record RotatedRefreshToken(User user, String rawToken, RefreshToken entity) {
    }
}
