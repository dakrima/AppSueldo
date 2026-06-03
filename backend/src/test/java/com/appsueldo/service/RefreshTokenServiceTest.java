package com.appsueldo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appsueldo.entity.RefreshToken;
import com.appsueldo.entity.User;
import com.appsueldo.exception.InvalidRefreshTokenException;
import com.appsueldo.repository.RefreshTokenRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void createStoresOnlyHashAndReturnsRawToken() {
        RefreshTokenService service = new RefreshTokenService(refreshTokenRepository, 30_000);
        User user = new User();
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshTokenService.CreatedRefreshToken created = service.create(user);

        assertThat(created.rawToken()).isNotBlank();
        assertThat(created.entity().getTokenHash()).isEqualTo(service.hash(created.rawToken()));
        assertThat(created.entity().getTokenHash()).isNotEqualTo(created.rawToken());
        assertThat(created.entity().isRevoked()).isFalse();
    }

    @Test
    void rotateRevokesPreviousTokenAndCreatesNextToken() {
        RefreshTokenService service = new RefreshTokenService(refreshTokenRepository, 30_000);
        User user = new User();
        String rawToken = "refresh-token";
        RefreshToken current = validToken(user, service.hash(rawToken));
        when(refreshTokenRepository.findByTokenHashAndRevokedFalse(service.hash(rawToken))).thenReturn(Optional.of(current));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshTokenService.RotatedRefreshToken rotated = service.rotate(rawToken);

        assertThat(current.isRevoked()).isTrue();
        assertThat(rotated.user()).isSameAs(user);
        assertThat(rotated.rawToken()).isNotBlank();
        assertThat(rotated.entity().getTokenHash()).isEqualTo(service.hash(rotated.rawToken()));
    }

    @Test
    void rotateRejectsExpiredTokenAndRevokesIt() {
        RefreshTokenService service = new RefreshTokenService(refreshTokenRepository, 30_000);
        String rawToken = "refresh-token";
        RefreshToken expired = validToken(new User(), service.hash(rawToken));
        expired.setExpiresAt(Instant.now().minusSeconds(1));
        when(refreshTokenRepository.findByTokenHashAndRevokedFalse(service.hash(rawToken))).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> service.rotate(rawToken))
            .isInstanceOf(InvalidRefreshTokenException.class);
        assertThat(expired.isRevoked()).isTrue();
    }

    @Test
    void revokeMarksTokenAsRevokedWhenPresent() {
        RefreshTokenService service = new RefreshTokenService(refreshTokenRepository, 30_000);
        String rawToken = "refresh-token";
        RefreshToken token = validToken(new User(), service.hash(rawToken));
        when(refreshTokenRepository.findByTokenHashAndRevokedFalse(service.hash(rawToken))).thenReturn(Optional.of(token));

        service.revoke(rawToken);

        assertThat(token.isRevoked()).isTrue();
        verify(refreshTokenRepository).findByTokenHashAndRevokedFalse(service.hash(rawToken));
    }

    private RefreshToken validToken(User user, String tokenHash) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setExpiresAt(Instant.now().plusSeconds(60));
        refreshToken.setRevoked(false);
        return refreshToken;
    }
}
