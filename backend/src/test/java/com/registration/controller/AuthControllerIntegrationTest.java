package com.registration.controller;

import com.registration.entity.User;
import com.registration.repository.UserRepository;
import com.registration.repository.EmailVerificationTokenRepository;
import com.registration.entity.EmailVerificationToken;
import com.registration.service.CaptchaService;
import com.registration.service.TwoFactorAuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @org.springframework.boot.test.context.TestConfiguration
    static class NoMailConfig {
        @org.springframework.context.annotation.Bean
        public org.springframework.mail.javamail.JavaMailSender mailSender() {
            // simple Mockito stub that does nothing when send() is called
            return Mockito.mock(org.springframework.mail.javamail.JavaMailSender.class);
        }

        @org.springframework.context.annotation.Bean
        @org.springframework.context.annotation.Primary
        public CaptchaService captchaService() {
            // always pass CAPTCHA in tests
            CaptchaService mock = Mockito.mock(CaptchaService.class);
            Mockito.when(mock.verify(Mockito.any(), Mockito.any())).thenReturn(true);
            return mock;
        }
    }

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TwoFactorAuthenticationService twoFAService;

    @BeforeEach
    void clean() {
        userRepository.deleteAll();
        emailVerificationTokenRepository.deleteAll();
    }

    private User buildBaseUser(String email, String rawPassword) {
        return User.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .isEmailVerified(true)
                .isTwoFAEnabled(false)
                .build();
    }

    @Test
    void loginWithout2FA_returnsToken() throws Exception {
        User u = buildBaseUser("nofa@example.com", "Password1!");
        userRepository.save(u);

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nofa@example.com\",\"password\":\"Password1!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.requiresTwoFA").value(false));
    }

    @Test
    void verifyEmail_enables2FA_thenLoginRequiresCode() throws Exception {
        String email = "verify@example.com";
        String password = "Password1!";

        User u = buildBaseUser(email, password);
        u.setIsEmailVerified(false);
        userRepository.save(u);

        // create token that will be consumed
        EmailVerificationToken token = EmailVerificationToken.builder()
                .token("tkn123")
                .expiryTime(LocalDateTime.now().plusHours(1))
                .user(u)
                .build();
        emailVerificationTokenRepository.save(token);

        // verify via controller (simulate whitespace around token)
        mvc.perform(post("/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"  tkn123  \"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isEmailVerified").value(true));

        // now login should trigger 2FA code
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requiresTwoFA").value(true));
    }

    @Test
    void loginWith2FA_viaEmailCode() throws Exception {
        String email = "twofa@example.com";
        String password = "Secret2!";

        User u = buildBaseUser(email, password);
        // enable 2FA but leave secret null so only email code will be used
        u.setIsTwoFAEnabled(true);
        userRepository.save(u);

        // perform initial login
        MvcResult first = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requiresTwoFA").value(true))
                .andExpect(jsonPath("$.message").value(containsString("code sent")))
                .andReturn();

        // after login the repository should contain a code
        User saved = userRepository.findByEmail(email).orElseThrow();
        String code = saved.getTwoFAEmailCode();
        assertThat(code).isNotNull().matches("\\d{6}");

        // verify using the returned code
        mvc.perform(post("/auth/2fa/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"code\":\"" + code + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void loginWith2FA_wrongCode_fails() throws Exception {
        String email = "twofail@example.com";
        String password = "Secret3!";

        User u = buildBaseUser(email, password);
        u.setIsTwoFAEnabled(true);
        userRepository.save(u);

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requiresTwoFA").value(true));

        mvc.perform(post("/auth/2fa/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"code\":\"000000\"}"))
                .andExpect(status().is4xxClientError());
    }
}
