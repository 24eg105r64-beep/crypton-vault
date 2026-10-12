package com.crypton.security;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

/**
 * Issues and verifies signed access tokens for the React frontend (/api/**).
 *
 * Token format: base64url("userId:expiresAtEpochSeconds") + "." + base64url(HMAC-SHA256 signature)
 *
 * Tokens are stateless: they cannot be revoked before they expire. Signing out
 * on the frontend simply discards the token. The secret comes from the
 * APP_TOKEN_SECRET environment variable in production.
 */
@Slf4j
@Service
public class ApiTokenService {

    private static final String DEV_SECRET = "local-development-only-secret-change-me";
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final String secret;
    private final long ttlSeconds;

    public ApiTokenService(@Value("${app.token-secret:" + DEV_SECRET + "}") String secret,
                           @Value("${app.token-ttl-hours:8}") long ttlHours) {
        this.secret = secret;
        this.ttlSeconds = ttlHours * 3600;
    }

    @PostConstruct
    void warnIfDefaultSecret() {
        if (DEV_SECRET.equals(secret)) {
            log.warn("APP_TOKEN_SECRET is not set. Using the development token secret. "
                    + "Set APP_TOKEN_SECRET to a long random value in production.");
        }
    }

    public String issueToken(Long userId) {
        long expiresAt = Instant.now().getEpochSecond() + ttlSeconds;
        String payload = encode((userId + ":" + expiresAt).getBytes(StandardCharsets.UTF_8));
        return payload + "." + encode(sign(payload));
    }

    /** Returns the user id if the token is genuine and not expired. */
    public Optional<Long> verifyToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        int dot = token.indexOf('.');
        if (dot <= 0 || dot == token.length() - 1) {
            return Optional.empty();
        }
        String payload = token.substring(0, dot);
        String signature = token.substring(dot + 1);
        try {
            byte[] expected = sign(payload);
            byte[] actual = Base64.getUrlDecoder().decode(signature);
            if (!MessageDigest.isEqual(expected, actual)) {
                return Optional.empty();
            }
            String decoded = new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            if (parts.length != 2) {
                return Optional.empty();
            }
            long expiresAt = Long.parseLong(parts[1]);
            if (Instant.now().getEpochSecond() > expiresAt) {
                return Optional.empty();
            }
            return Optional.of(Long.parseLong(parts[0]));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private byte[] sign(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Could not sign API token", e);
        }
    }

    private static String encode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
