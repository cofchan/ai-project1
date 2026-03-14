package com.registration.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class CaptchaService {

    @Value("${captcha.secret:change-this-captcha-secret-in-production}")
    private String secret;

    private static final long TTL_SECONDS = 300; // 5 minutes

    private static final String CHARSET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 4;

    public record Challenge(String captchaToken, String captchaQuestion) {}

    /**
     * Generates a 4-character alphanumeric CAPTCHA challenge and returns a signed token.
     */
    public Challenge generateChallenge() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARSET.charAt(ThreadLocalRandom.current().nextInt(CHARSET.length())));
        }
        String captchaCode = code.toString();
        long timestamp = Instant.now().getEpochSecond();
        String data = captchaCode + ":" + timestamp;
        String raw = data + ":" + hmacHex(data);
        String token = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        return new Challenge(token, captchaCode);
    }

    /**
     * Verifies a CAPTCHA token and user-provided answer (case-insensitive).
     */
    public boolean verify(String encodedToken, String answer) {
        if (encodedToken == null || encodedToken.isBlank() || answer == null || answer.isBlank()) {
            return false;
        }
        try {
            String raw = new String(Base64.getUrlDecoder().decode(encodedToken), StandardCharsets.UTF_8);
            // format: CODE:timestamp:hmac
            int lastColon = raw.lastIndexOf(':');
            if (lastColon < 0) return false;
            String sig = raw.substring(lastColon + 1);
            String data = raw.substring(0, lastColon);

            int firstColon = data.indexOf(':');
            if (firstColon < 0) return false;
            String captchaCode = data.substring(0, firstColon);
            long timestamp = Long.parseLong(data.substring(firstColon + 1));

            // Check expiry
            if (Instant.now().getEpochSecond() - timestamp > TTL_SECONDS) {
                log.debug("CAPTCHA token expired");
                return false;
            }

            // Verify HMAC signature
            if (!hmacHex(data).equals(sig)) {
                log.debug("CAPTCHA HMAC mismatch");
                return false;
            }

            // Verify answer (case-insensitive)
            return captchaCode.equalsIgnoreCase(answer.trim());
        } catch (Exception e) {
            log.debug("CAPTCHA verification error", e);
            return false;
        }
    }

    private String hmacHex(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("HMAC computation failed", e);
        }
    }
}
