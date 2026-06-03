package com.appsueldo.controller;

import com.appsueldo.dto.AuthUserResponse;
import com.appsueldo.dto.LoginRequest;
import com.appsueldo.dto.RegisterRequest;
import com.appsueldo.entity.User;
import com.appsueldo.security.AuthCookieService;
import com.appsueldo.security.JwtService;
import com.appsueldo.service.AuthService;
import com.appsueldo.service.CurrentUserService;
import com.appsueldo.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthCookieService authCookieService;
    private final CurrentUserService currentUserService;

    public AuthController(
        AuthService authService,
        JwtService jwtService,
        RefreshTokenService refreshTokenService,
        AuthCookieService authCookieService,
        CurrentUserService currentUserService
    ) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.authCookieService = authCookieService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/google")
    public ResponseEntity<Void> loginWithGoogle() {
        return ResponseEntity.status(HttpStatus.FOUND)
            .header(HttpHeaders.LOCATION, "/oauth2/authorization/google")
            .build();
    }

    @PostMapping("/register")
    public AuthUserResponse register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        User user = authService.register(request);
        issueCookies(user, response);
        return AuthUserResponse.from(user);
    }

    @PostMapping("/login")
    public AuthUserResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        User user = authService.login(request);
        issueCookies(user, response);
        return AuthUserResponse.from(user);
    }

    @PostMapping("/refresh")
    public AuthUserResponse refresh(
        @CookieValue(name = AuthCookieService.REFRESH_COOKIE, required = false) String refreshToken,
        HttpServletResponse response
    ) {
        RefreshTokenService.RotatedRefreshToken rotated = refreshTokenService.rotate(refreshToken);
        String accessToken = jwtService.createAccessToken(rotated.user());
        authCookieService.addAuthCookies(response, accessToken, rotated.rawToken());
        return AuthUserResponse.from(rotated.user());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
        @CookieValue(name = AuthCookieService.REFRESH_COOKIE, required = false) String refreshToken,
        HttpServletResponse response
    ) {
        refreshTokenService.revoke(refreshToken);
        authCookieService.clearAuthCookies(response);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(HttpServletResponse response) {
        refreshTokenService.revokeAll(currentUserService.currentUser());
        authCookieService.clearAuthCookies(response);
        return ResponseEntity.noContent().build();
    }

    private void issueCookies(User user, HttpServletResponse response) {
        String accessToken = jwtService.createAccessToken(user);
        RefreshTokenService.CreatedRefreshToken refreshToken = refreshTokenService.create(user);
        authCookieService.addAuthCookies(response, accessToken, refreshToken.rawToken());
    }
}
