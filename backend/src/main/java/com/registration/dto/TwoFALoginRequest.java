package com.registration.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TwoFALoginRequest {
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "2FA code is required")
    private String code;
}
