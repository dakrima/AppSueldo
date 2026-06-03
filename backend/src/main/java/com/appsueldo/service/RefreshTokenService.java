package com.appsueldo.service;

import com.appsueldo.entity.RefreshToken;
import com.appsueldo.entity.User;
import com.appsueldo.repository.RefreshTokenRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long expirationMillis;

    public RefreshTokenService(
        RefreshTokenRepository refreshTokenRepository,
        @Value("${app.refresh-token.expiration}") long expirationMillis
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.expirationMillis = expirationMillis;
    }

    @Transactional
    public RefreshToken create(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(Instant.now().plusMillis(expirationMillis));
        refreshToken.setRevoked(false);
        return refreshTokenRepository.save(refreshToken);
    }
}
