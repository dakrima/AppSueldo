package com.appsueldo.service;

import com.appsueldo.dto.LoginRequest;
import com.appsueldo.dto.RegisterRequest;
import com.appsueldo.entity.User;
import com.appsueldo.exception.BadRequestException;
import com.appsueldo.exception.ConflictException;
import com.appsueldo.exception.InvalidCredentialsException;
import com.appsueldo.repository.UserRepository;
import java.util.Locale;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        validatePassword(request.password());

        if (userRepository.findByEmail(email).isPresent()) {
            throw new ConflictException("Ese email ya esta registrado.");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEmailVerified(false);

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new InvalidCredentialsException("Credenciales invalidas."));

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Credenciales invalidas.");
        }

        return user;
    }

    @Transactional
    public User upsertGoogleUser(Map<String, Object> attributes) {
        String googleId = requiredAttribute(attributes, "sub");
        String email = normalizeEmail(requiredAttribute(attributes, "email"));
        String name = (String) attributes.getOrDefault("name", email);
        String pictureUrl = (String) attributes.get("picture");
        boolean emailVerified = Boolean.TRUE.equals(attributes.get("email_verified"));

        User user = userRepository.findByGoogleId(googleId).orElse(null);

        if (user == null) {
            user = userRepository.findByEmail(email).orElse(null);
            if (user != null && user.getGoogleId() == null && !emailVerified) {
                throw new InvalidCredentialsException("Google no verifico el email de la cuenta.");
            }
            if (user == null) {
                user = new User();
                user.setEmail(email);
            }
            user.setGoogleId(googleId);
        }

        user.setEmail(email);
        user.setName(name);
        user.setPictureUrl(pictureUrl);
        user.setEmailVerified(user.isEmailVerified() || emailVerified);

        return userRepository.save(user);
    }

    private void validatePassword(String password) {
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        if (!hasLetter || !hasDigit) {
            throw new BadRequestException("La contrasena debe incluir letras y numeros.");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String requiredAttribute(Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);
        if (value == null || value.toString().isBlank()) {
            throw new InvalidCredentialsException("Google no retorno el atributo requerido: " + key);
        }
        return value.toString();
    }
}
