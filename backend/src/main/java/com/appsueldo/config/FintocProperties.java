package com.appsueldo.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.fintoc")
public record FintocProperties(
    @NotBlank String secretKey,
    @NotBlank String publicKey,
    @NotBlank String baseUrl,
    @NotBlank String tokenEncryptionKey,
    @NotBlank String env,
    String webhookSecret
) {
    public FintocProperties {
        secretKey = trim(secretKey);
        publicKey = trim(publicKey);
        baseUrl = stripTrailingSlash(trim(baseUrl));
        tokenEncryptionKey = trim(tokenEncryptionKey);
        env = trim(env);
        webhookSecret = webhookSecret == null ? "" : webhookSecret.trim();
    }

    @Override
    public String toString() {
        return "FintocProperties[secretKey=****, publicKey=" + publicKey
            + ", baseUrl=" + baseUrl
            + ", tokenEncryptionKey=****, env=" + env
            + ", webhookSecret=" + (webhookSecret.isBlank() ? "" : "****") + "]";
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private static String stripTrailingSlash(String value) {
        if (value == null || value.length() <= 1) {
            return value;
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
