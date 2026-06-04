package com.appsueldo.service.fintoc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.appsueldo.config.FintocProperties;
import org.junit.jupiter.api.Test;

class FintocTokenCryptoTest {

    @Test
    void encryptsAndDecryptsLinkToken() {
        FintocTokenCrypto crypto = new FintocTokenCrypto(validProperties());

        String encryptedToken = crypto.encrypt("link_token_secret");

        assertThat(encryptedToken).startsWith("v1:");
        assertThat(encryptedToken).isNotEqualTo("link_token_secret");
        assertThat(encryptedToken).doesNotContain("link_token_secret");
        assertThat(crypto.decrypt(encryptedToken)).isEqualTo("link_token_secret");
    }

    @Test
    void failsWithInvalidEncryptionKey() {
        FintocProperties properties = new FintocProperties(
            "sk_test_secret",
            "pk_test_public",
            "https://api.fintoc.com",
            "not-base64",
            "test",
            "whsec_secret"
        );

        assertThatThrownBy(() -> new FintocTokenCrypto(properties))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("FINTOC_TOKEN_ENCRYPTION_KEY must be base64-encoded 32 bytes.");
    }

    @Test
    void failsWithInvalidEncryptedTokenWithoutLeakingInput() {
        FintocTokenCrypto crypto = new FintocTokenCrypto(validProperties());

        assertThatThrownBy(() -> crypto.decrypt("plain_link_token"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Encrypted Fintoc token is invalid.");
    }

    private FintocProperties validProperties() {
        return new FintocProperties(
            "sk_test_secret",
            "pk_test_public",
            "https://api.fintoc.com",
            "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
            "test",
            "whsec_secret"
        );
    }
}
