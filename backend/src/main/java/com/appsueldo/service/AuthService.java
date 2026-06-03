package com.appsueldo.service;

import com.appsueldo.entity.User;
import com.appsueldo.repository.UserRepository;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User upsertGoogleUser(Map<String, Object> attributes) {
        String googleId = requiredAttribute(attributes, "sub");
        String email = requiredAttribute(attributes, "email");
        String name = (String) attributes.getOrDefault("name", email);
        String pictureUrl = (String) attributes.get("picture");

        User user = userRepository.findByGoogleId(googleId)
            .or(() -> userRepository.findByEmail(email))
            .orElseGet(User::new);

        user.setGoogleId(googleId);
        user.setEmail(email);
        user.setName(name);
        user.setPictureUrl(pictureUrl);

        return userRepository.save(user);
    }

    private String requiredAttribute(Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);
        if (value == null || value.toString().isBlank()) {
            throw new IllegalArgumentException("Google no retorno el atributo requerido: " + key);
        }
        return value.toString();
    }
}
