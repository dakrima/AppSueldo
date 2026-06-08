package com.appsueldo.service.fintoc;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.appsueldo.config.FintocProperties;
import com.appsueldo.exception.BadRequestException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;

class FintocWebhookSignatureVerifierTest {

    private static final String SECRET = "whsec_secret";
    private static final Instant NOW = Instant.parse("2026-06-04T12:00:00Z");

    @Test
    void acceptsValidFintocSignature() {
        FintocWebhookSignatureVerifier verifier = verifier();
        String rawBody = "{\"id\":\"evt_123\",\"type\":\"account.refresh_intent.succeeded\"}";
        String timestamp = Long.toString(NOW.getEpochSecond());
        String signature = hmac(timestamp + "." + rawBody);

        verifier.verify(rawBody, "t=" + timestamp + ",v1=" + signature);
    }

    @Test
    void rejectsInvalidSignature() {
        FintocWebhookSignatureVerifier verifier = verifier();
        String rawBody = "{\"id\":\"evt_123\"}";
        String timestamp = Long.toString(NOW.getEpochSecond());

        assertThatThrownBy(() -> verifier.verify(rawBody, "t=" + timestamp + ",v1=invalid"))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("Firma Fintoc invalida.");
    }

    @Test
    void rejectsOldTimestamp() {
        FintocWebhookSignatureVerifier verifier = verifier();
        String rawBody = "{\"id\":\"evt_123\"}";
        String timestamp = Long.toString(NOW.minusSeconds(3600).getEpochSecond());
        String signature = hmac(timestamp + "." + rawBody);

        assertThatThrownBy(() -> verifier.verify(rawBody, "t=" + timestamp + ",v1=" + signature))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("Firma Fintoc invalida.");
    }

    @Test
    void rejectsVerificationWhenWebhookSecretIsBlank() {
        FintocWebhookSignatureVerifier verifier = verifierWithSecret("");

        assertThatThrownBy(() -> verifier.verify("{}", "t=" + NOW.getEpochSecond() + ",v1=signature"))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("Firma Fintoc invalida.");
    }

    private FintocWebhookSignatureVerifier verifier() {
        return verifierWithSecret(SECRET);
    }

    private FintocWebhookSignatureVerifier verifierWithSecret(String webhookSecret) {
        return new FintocWebhookSignatureVerifier(
            new FintocProperties(
                "sk_test_secret",
                "pk_test_public",
                "https://api.fintoc.com",
                "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
                "test",
                90,
                webhookSecret
            ),
            Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    private String hmac(String message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] bytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("Could not sign test payload.", exception);
        }
    }
}
