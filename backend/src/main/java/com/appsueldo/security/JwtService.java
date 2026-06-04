package com.appsueldo.security;

import com.appsueldo.entity.User;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final long expirationMillis;

    public JwtService(JwtEncoder jwtEncoder, @Value("${app.jwt.expiration}") long expirationMillis) {
        this.jwtEncoder = jwtEncoder;
        this.expirationMillis = expirationMillis;
    }

    public String createAccessToken(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("appsueldo")
            .issuedAt(now)
            .expiresAt(now.plusMillis(expirationMillis))
            .subject(user.getId().toString())
            .claim("email", user.getEmail())
            .claim("name", user.getName())
            .claim("scope", "USER")
            .build();

        JwsHeader headers = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();
    }

    public long getExpirationMillis() {
        return expirationMillis;
    }
}
