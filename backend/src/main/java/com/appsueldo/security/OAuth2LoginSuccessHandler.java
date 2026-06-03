package com.appsueldo.security;

import com.appsueldo.entity.RefreshToken;
import com.appsueldo.entity.User;
import com.appsueldo.service.AuthService;
import com.appsueldo.service.RefreshTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final String frontendUrl;

    public OAuth2LoginSuccessHandler(
        AuthService authService,
        JwtService jwtService,
        RefreshTokenService refreshTokenService,
        @Value("${app.frontend-url}") String frontendUrl
    ) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        User user = authService.upsertGoogleUser(oauth2User.getAttributes());
        String accessToken = jwtService.createAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.create(user);

        String redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl)
            .path("/dashboard")
            .queryParam("accessToken", accessToken)
            .queryParam("refreshToken", refreshToken.getToken())
            .build()
            .toUriString();

        response.sendRedirect(redirectUrl);
    }
}
