package com.appsueldo.repository;

import com.appsueldo.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByGoogleId(String googleId);

    Optional<User> findByEmail(String email);
}
