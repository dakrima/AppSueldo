package com.appsueldo.service;

import com.appsueldo.entity.User;
import com.appsueldo.repository.UserRepository;
import java.util.Map;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;
    private final AuthService authService;

    public CurrentUserService(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @Transactional
    public User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadCredentialsException("Usuario no autenticado.");
        }

        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            Jwt jwt = jwtAuthenticationToken.getToken();
            return userRepository.findById(Long.valueOf(jwt.getSubject()))
                .orElseThrow(() -> new BadCredentialsException("Usuario autenticado no existe."));
        }

        if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            Map<String, Object> attributes = oauth2User.getAttributes();
            return authService.upsertGoogleUser(attributes);
        }

        throw new BadCredentialsException("Tipo de autenticacion no soportado.");
    }
}
