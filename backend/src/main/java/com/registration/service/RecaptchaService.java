package com.registration.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/** @deprecated Replaced by {@link CaptchaService}. Kept as dead code; not a Spring bean. */
@Slf4j
public class RecaptchaService {

    private String secretKey = "";

    private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Verifies a reCAPTCHA v2 token against Google's API.
     *
     * @param token the reCAPTCHA response token from the client
     * @return true if the token is valid, false otherwise
     */
    public boolean verify(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("secret", secretKey);
            params.add("response", token);

            @SuppressWarnings("rawtypes")
            Map body = restTemplate.postForObject(VERIFY_URL, params, Map.class);
            return body != null && Boolean.TRUE.equals(body.get("success"));
        } catch (Exception e) {
            log.error("reCAPTCHA verification request failed", e);
            return false;
        }
    }
}
