package com.appsueldo.service.fintoc;

import com.appsueldo.config.FintocProperties;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

@Component
public class FintocTokenCrypto {

    private static final String VERSION = "v1";
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_LENGTH_BYTES = 32;
    private static final int NONCE_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final SecretKeySpec secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public FintocTokenCrypto(FintocProperties properties) {
        this.secretKey = new SecretKeySpec(decodeKey(properties.tokenEncryptionKey()), ALGORITHM);
    }

    public String encrypt(String plainToken) {
        if (plainToken == null || plainToken.isBlank()) {
            throw new IllegalArgumentException("Fintoc token must not be blank.");
        }

        byte[] nonce = new byte[NONCE_LENGTH_BYTES];
        secureRandom.nextBytes(nonce);

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BITS, nonce));
            byte[] ciphertext = cipher.doFinal(plainToken.getBytes(StandardCharsets.UTF_8));

            return VERSION
                + ":"
                + Base64.getEncoder().encodeToString(nonce)
                + ":"
                + Base64.getEncoder().encodeToString(ciphertext);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Could not encrypt Fintoc token.");
        }
    }

    public String decrypt(String encryptedToken) {
        if (encryptedToken == null || encryptedToken.isBlank()) {
            throw new IllegalArgumentException("Encrypted Fintoc token must not be blank.");
        }

        String[] parts = encryptedToken.split(":", -1);
        if (parts.length != 3 || !VERSION.equals(parts[0])) {
            throw new IllegalArgumentException("Encrypted Fintoc token is invalid.");
        }

        try {
            byte[] nonce = Base64.getDecoder().decode(parts[1]);
            byte[] ciphertext = Base64.getDecoder().decode(parts[2]);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BITS, nonce));
            byte[] plainToken = cipher.doFinal(ciphertext);
            return new String(plainToken, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException | GeneralSecurityException exception) {
            throw new IllegalArgumentException("Encrypted Fintoc token is invalid.");
        }
    }

    private byte[] decodeKey(String encodedKey) {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
            if (decodedKey.length != KEY_LENGTH_BYTES) {
                throw new IllegalArgumentException();
            }
            return decodedKey;
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("FINTOC_TOKEN_ENCRYPTION_KEY must be base64-encoded 32 bytes.");
        }
    }
}
