package com.registration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TwoFASetupResponse {
    private String secret;
    private byte[] qrCode; // Base64 encoded QR code
    private String qrCodeBase64;
    private String[] backupCodes;
    private String setupUri;
    private String message;
}
