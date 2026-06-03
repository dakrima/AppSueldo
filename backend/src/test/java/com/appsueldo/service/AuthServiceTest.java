package com.appsueldo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.appsueldo.dto.LoginRequest;
import com.appsueldo.dto.RegisterRequest;
import com.appsueldo.entity.User;
import com.appsueldo.exception.ConflictException;
import com.appsueldo.exception.InvalidCredentialsException;
import com.appsueldo.repository.UserRepository;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void registerCreatesLocalUserWithHashedPassword() {
        AuthService authService = new AuthService(userRepository, passwordEncoder);
        when(userRepository.findByEmail("david@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = authService.register(new RegisterRequest("David Kripper", "DAVID@example.com", "password123"));

        assertThat(user.getEmail()).isEqualTo("david@example.com");
        assertThat(user.getName()).isEqualTo("David Kripper");
        assertThat(user.getGoogleId()).isNull();
        assertThat(user.getPasswordHash()).isNotBlank();
        assertThat(passwordEncoder.matches("password123", user.getPasswordHash())).isTrue();
        assertThat(user.isEmailVerified()).isFalse();
    }

    @Test
    void registerRejectsDuplicatedEmail() {
        AuthService authService = new AuthService(userRepository, passwordEncoder);
        when(userRepository.findByEmail("david@example.com")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authService.register(new RegisterRequest("David", "david@example.com", "password123")))
            .isInstanceOf(ConflictException.class);
    }

    @Test
    void loginReturnsUserWhenPasswordMatches() {
        AuthService authService = new AuthService(userRepository, passwordEncoder);
        User user = new User();
        user.setEmail("david@example.com");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        when(userRepository.findByEmail("david@example.com")).thenReturn(Optional.of(user));

        User loggedIn = authService.login(new LoginRequest("david@example.com", "password123"));

        assertThat(loggedIn).isSameAs(user);
    }

    @Test
    void loginRejectsInvalidPassword() {
        AuthService authService = new AuthService(userRepository, passwordEncoder);
        User user = new User();
        user.setEmail("david@example.com");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        when(userRepository.findByEmail("david@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("david@example.com", "wrong123")))
            .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void loginRejectsGoogleOnlyUserWithoutLocalPassword() {
        AuthService authService = new AuthService(userRepository, passwordEncoder);
        User user = new User();
        user.setEmail("david@example.com");
        user.setGoogleId("google-123");
        when(userRepository.findByEmail("david@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("david@example.com", "password123")))
            .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void googleLoginLinksExistingLocalUserWhenEmailIsVerified() {
        AuthService authService = new AuthService(userRepository, passwordEncoder);
        User user = new User();
        user.setEmail("david@example.com");
        user.setName("David");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        when(userRepository.findByGoogleId("google-123")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("david@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User linked = authService.upsertGoogleUser(Map.of(
            "sub", "google-123",
            "email", "DAVID@example.com",
            "email_verified", true,
            "name", "David Google",
            "picture", "https://example.com/picture.jpg"
        ));

        assertThat(linked).isSameAs(user);
        assertThat(linked.getGoogleId()).isEqualTo("google-123");
        assertThat(linked.getEmail()).isEqualTo("david@example.com");
        assertThat(linked.getName()).isEqualTo("David Google");
        assertThat(linked.getPictureUrl()).isEqualTo("https://example.com/picture.jpg");
        assertThat(linked.isEmailVerified()).isTrue();
        assertThat(passwordEncoder.matches("password123", linked.getPasswordHash())).isTrue();
    }

    @Test
    void googleLoginRejectsExistingLocalUserWhenEmailIsNotVerified() {
        AuthService authService = new AuthService(userRepository, passwordEncoder);
        User user = new User();
        user.setEmail("david@example.com");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        when(userRepository.findByGoogleId("google-123")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("david@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.upsertGoogleUser(Map.of(
            "sub", "google-123",
            "email", "david@example.com",
            "email_verified", false
        ))).isInstanceOf(InvalidCredentialsException.class);
    }
}
