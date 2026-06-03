package com.appsueldo.repository;

import com.appsueldo.entity.RefreshToken;
import com.appsueldo.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHashAndRevokedFalse(String tokenHash);

    List<RefreshToken> findByUserAndRevokedFalse(User user);
}
