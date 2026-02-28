package com.registration.service;

import com.google.common.io.BaseEncoding;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.*;

@Service
@Slf4j
public class TwoFactorAuthenticationService {

    private static final String ALGORITHM = "HmacSHA1";
    private static final int TIME_STEP = 30; // 30 seconds
    private static final int DIGITS = 6;
    private static final long TOTP_WINDOW = 1; // Number of windows to check before/after

    /**
     * Generate a new secret key for 2FA
     */
    public String generateSecret() {
        try {
            KeyGenerator keygen = KeyGenerator.getInstance("HmacSHA1");
            keygen.init(160);
            SecretKey secretKey = keygen.generateKey();
            return BaseEncoding.base32().encode(secretKey.getEncoded());
        } catch (Exception e) {
            log.error("Failed to generate 2FA secret", e);
            throw new RuntimeException("Failed to generate 2FA secret", e);
        }
    }

    /**
     * Verify TOTP code
     */
    public boolean verifyCode(String secret, String code) {
        try {
            long timeIndex = System.currentTimeMillis() / 1000 / TIME_STEP;
            
            // Check current window and ±1 to handle time drift
            for (long i = -TOTP_WINDOW; i <= TOTP_WINDOW; i++) {
                String expectedCode = generateHOTP(secret, timeIndex + i);
                if (expectedCode.equals(code)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("Failed to verify 2FA code", e);
            return false;
        }
    }

    /**
     * Generate HOTP code
     */
    private String generateHOTP(String secret, long counter) {
        try {
            byte[] decodedSecret = BaseEncoding.base32().decode(secret);
            byte[] counterBytes = ByteBuffer.allocate(8).putLong(counter).array();

            HmacUtils hmac = new HmacUtils(HmacAlgorithms.HMAC_SHA_1, decodedSecret);
            byte[] hash = hmac.digest(counterBytes);

            // Dynamic truncation
            int offset = hash[hash.length - 1] & 0xf;
            int truncatedHash = 0;
            for (int i = 0; i < 4; ++i) {
                truncatedHash <<= 8;
                truncatedHash |= (hash[offset + i] & 0xff);
            }
            truncatedHash &= 0x7fffffff;
            truncatedHash %= 1000000;

            return String.format("%0" + DIGITS + "d", truncatedHash);
        } catch (Exception e) {
            log.error("Failed to generate HOTP", e);
            throw new RuntimeException("Failed to generate HOTP", e);
        }
    }

    /**
     * Generate QR code for authenticator app setup
     */
    public byte[] generateQRCode(String email, String secret) {
        try {
            String otpAuthUrl = String.format(
                    "otpauth://totp/%s?secret=%s&issuer=UserRegistration",
                    email,
                    secret
            );

            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    otpAuthUrl,
                    BarcodeFormat.QR_CODE,
                    200,
                    200
            );

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate QR code", e);
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    /**
     * Generate backup codes for account recovery
     */
    public List<String> generateBackupCodes() {
        List<String> backupCodes = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 0; i < 10; i++) {
            StringBuilder code = new StringBuilder();
            for (int j = 0; j < 8; j++) {
                code.append(String.format("%04d", random.nextInt(10000)));
                if (j == 3) {
                    code.append("-");
                }
            }
            backupCodes.add(code.toString());
        }
        
        return backupCodes;
    }

    /**
     * Verify and consume backup code
     */
    public boolean verifyBackupCode(String backupCodesJson, String code) {
        try {
            if (backupCodesJson == null || backupCodesJson.isEmpty()) {
                return false;
            }

            List<String> codes = Arrays.asList(backupCodesJson.split(","));
            return codes.contains(code.trim());
        } catch (Exception e) {
            log.error("Failed to verify backup code", e);
            return false;
        }
    }

    /**
     * Remove backup code after use
     */
    public String removeBackupCode(String backupCodesJson, String code) {
        try {
            List<String> codes = new ArrayList<>(Arrays.asList(backupCodesJson.split(",")));
            codes.remove(code.trim());
            return String.join(",", codes);
        } catch (Exception e) {
            log.error("Failed to remove backup code", e);
            throw new RuntimeException("Failed to remove backup code", e);
        }
    }

    /**
     * Convert backup codes list to JSON string
     */
    public String backupCodesToJson(List<String> codes) {
        return String.join(",", codes);
    }

    /**
     * Convert JSON string to backup codes list
     */
    public List<String> jsonToBackupCodes(String json) {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(json.split(","));
    }
}
