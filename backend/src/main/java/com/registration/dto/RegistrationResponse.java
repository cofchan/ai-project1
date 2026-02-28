package com.registration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationResponse {
    private Long id;
    private String email;
    private String fullName;
    private String message;

    @JsonProperty("isEmailVerified")
    private Boolean isEmailVerified;
}
