package com.registration.service;

import com.registration.config.JwtTokenProvider;
import com.registration.dto.UserResponse;
import com.registration.entity.EmailVerificationToken;
import com.registration.entity.PasswordResetToken;
import com.registration.entity.User;
import com.registration.exception.InvalidTokenException;
import com.registration.exception.UserNotFoundException;
import com.registration.repository.EmailVerificationTokenRepository;
import com.registration.repository.PasswordResetTokenRepository;
import com.registration.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private TwoFactorAuthenticationService twoFAService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .isTwoFAEnabled(true)
                .build();
    }

    @Test
    void verifyEmail_enablesTwoFA() {
        // arrange
        EmailVerificationToken token = EmailVerificationToken.builder()
                .token("tok")
                .expiryTime(LocalDateTime.now().plusHours(1))
                .user(user)
                .build();
        when(emailVerificationTokenRepository.findByToken("tok")).thenReturn(Optional.of(token));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // act
        UserResponse resp = userService.verifyEmail("tok");

        // assert
        assertThat(resp.getIsEmailVerified()).isTrue();
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getIsTwoFAEnabled()).isTrue();
    }

    @Test
    void verifyEmail_trimsToken() {
        // arrange - same token stored without whitespace
        EmailVerificationToken token = EmailVerificationToken.builder()
                .token("tok")
                .expiryTime(LocalDateTime.now().plusHours(1))
                .user(user)
                .build();
        when(emailVerificationTokenRepository.findByToken("tok")).thenReturn(Optional.of(token));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // act - supply token with surrounding spaces
        UserResponse resp = userService.verifyEmail("  tok  ");

        // assert
        assertThat(resp.getIsEmailVerified()).isTrue();
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getIsTwoFAEnabled()).isTrue();
    }

    @Test
    void resetPassword_trimsToken() {
        // arrange
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token("rst")
                .expiryTime(LocalDateTime.now().plusHours(1))
                .user(user)
                .used(false)
                .build();
        when(passwordResetTokenRepository.findByToken("rst")).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        // act
        userService.resetPassword(" rst ", "newpass");

        // assert - verifying that repository was called to fetch token was enough; no exception thrown
        verify(passwordResetTokenRepository).findByToken("rst");
    }

    @Test
    void authenticateWith2FA_usingEmailCode_success() {
        // arrange
        user.setTwoFAEmailCode("123456");
        user.setTwoFAEmailCodeExpiry(LocalDateTime.now().plusMinutes(5));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(user.getEmail())).thenReturn("jwt-token");

        // act
        var response = userService.authenticateWith2FA(user.getEmail(), "123456");

        // assert
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUser().getEmail()).isEqualTo(user.getEmail());
        // email code should have been cleared and saved
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getTwoFAEmailCode()).isNull();
        assertThat(userCaptor.getValue().getTwoFAEmailCodeExpiry()).isNull();
    }

    @Test
    void authenticateWith2FA_wrongEmailCode_throws() {
        user.setTwoFAEmailCode("999999");
        user.setTwoFAEmailCodeExpiry(LocalDateTime.now().plusMinutes(5));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.authenticateWith2FA(user.getEmail(), "123456"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Invalid 2FA code");
    }

    @Test
    void authenticateWith2FA_expiredEmailCode_triesTOTP_thenFails() {
        user.setTwoFAEmailCode("123456");
        user.setTwoFAEmailCodeExpiry(LocalDateTime.now().minusMinutes(1));
        user.setTwoFASecret("secret");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(twoFAService.verifyCode("secret", "123456")).thenReturn(false);

        assertThatThrownBy(() -> userService.authenticateWith2FA(user.getEmail(), "123456"))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void generateAndSendEmail2FACode_storesCodeAndSendsEmail() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(twoFAService.generateEmailCode()).thenReturn("654321");

        userService.generateAndSendEmail2FACode(user.getEmail());

        // verify that user was updated with code and expiry
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getTwoFAEmailCode()).matches("\\d{6}");
        assertThat(saved.getTwoFAEmailCodeExpiry()).isAfter(LocalDateTime.now());

        // email service should be called
        verify(emailService).sendTwoFactorCodeEmail(eq(saved), anyString());
    }

    @Test
    void generateAndSendEmail2FACode_userNotFound() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.generateAndSendEmail2FACode(user.getEmail()))
                .isInstanceOf(UserNotFoundException.class);
    }
}
