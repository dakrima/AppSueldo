package com.appsueldo.security;

import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class AuthCookieService {

    public static final String ACCESS_COOKIE = "access_token";
    public static final String REFRESH_COOKIE = "refresh_token";

    private final boolean secureCookies;
    private final String sameSite;
    private final long accessExpirationMillis;
    private final long refreshExpirationMillis;

    public AuthCookieService(
        @Value("${app.cookies.secure:false}") boolean secureCookies,
        @Value("${app.cookies.same-site:Lax}") String sameSite,
        @Value("${app.jwt.expiration}") long accessExpirationMillis,
        @Value("${app.refresh-token.expiration}") long refreshExpirationMillis
    ) {
        this.secureCookies = secureCookies;
        this.sameSite = sameSite;
        this.accessExpirationMillis = accessExpirationMillis;
        this.refreshExpirationMillis = refreshExpirationMillis;
    }

    public void addAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie(ACCESS_COOKIE, accessToken, accessExpirationMillis).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie(REFRESH_COOKIE, refreshToken, refreshExpirationMillis).toString());
    }

    public void clearAuthCookies(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie(ACCESS_COOKIE).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie(REFRESH_COOKIE).toString());
    }

    private ResponseCookie cookie(String name, String value, long expirationMillis) {
        return ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(secureCookies)
            .sameSite(sameSite)
            .path("/")
            .maxAge(Duration.ofMillis(expirationMillis))
            .build();
    }

    private ResponseCookie expiredCookie(String name) {
        return ResponseCookie.from(name, "")
            .httpOnly(true)
            .secure(secureCookies)
            .sameSite(sameSite)
            .path("/")
            .maxAge(Duration.ZERO)
            .build();
    }
}
