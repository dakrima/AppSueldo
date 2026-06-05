package com.appsueldo.service.fintoc;

import com.appsueldo.config.FintocProperties;
import com.appsueldo.exception.BadRequestException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

@Component
public class FintocWebhookSignatureVerifier {

    private static final Duration SIGNATURE_TOLERANCE = Duration.ofMinutes(5);
    private static final String SIGNATURE_ALGORITHM = "HmacSHA256";

    private final FintocProperties properties;
    private final Clock clock;

    public FintocWebhookSignatureVerifier(FintocProperties properties) {
        this(properties, Clock.systemUTC());
    }

    FintocWebhookSignatureVerifier(FintocProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    public void verify(String rawBody, String signatureHeader) {
        String secret = properties.webhookSecret();
        if (secret == null || secret.isBlank()) {
            throw new BadRequestException("Firma Fintoc invalida.");
        }
        if (rawBody == null || signatureHeader == null || signatureHeader.isBlank()) {
            throw new BadRequestException("Firma Fintoc invalida.");
        }

        String timestamp = headerValue(signatureHeader, "t");
        String receivedSignature = headerValue(signatureHeader, "v1");
        validateTimestamp(timestamp);

        String expectedSignature = hmacSha256(secret, timestamp + "." + rawBody);
        if (!MessageDigest.isEqual(
            expectedSignature.getBytes(StandardCharsets.UTF_8),
            receivedSignature.getBytes(StandardCharsets.UTF_8)
        )) {
            throw new BadRequestException("Firma Fintoc invalida.");
        }
    }

    private void validateTimestamp(String timestamp) {
        Instant signedAt;
        try {
            signedAt = Instant.ofEpochSecond(Long.parseLong(timestamp));
        } catch (NumberFormatException exception) {
            throw new BadRequestException("Firma Fintoc invalida.");
        }

        Duration age = Duration.between(signedAt, Instant.now(clock)).abs();
        if (age.compareTo(SIGNATURE_TOLERANCE) > 0) {
            throw new BadRequestException("Firma Fintoc invalida.");
        }
    }

    private String headerValue(String signatureHeader, String key) {
        return Arrays.stream(signatureHeader.split(","))
            .map(String::trim)
            .filter(part -> part.startsWith(key + "="))
            .map(part -> part.substring((key + "=").length()))
            .filter(value -> !value.isBlank())
            .findFirst()
            .orElseThrow(() -> new BadRequestException("Firma Fintoc invalida."));
    }

    private String hmacSha256(String secret, String message) {
        try {
            Mac mac = Mac.getInstance(SIGNATURE_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), SIGNATURE_ALGORITHM));
            return toHex(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo validar firma Fintoc.");
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(String.format("%02x", value));
        }
        return builder.toString();
    }
}
