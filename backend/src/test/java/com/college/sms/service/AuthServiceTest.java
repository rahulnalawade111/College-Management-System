package com.college.sms.service;

import com.college.sms.dto.LoginRequest;
import com.college.sms.dto.RegisterRequest;
import com.college.sms.entity.Role;
import com.college.sms.entity.User;
import com.college.sms.exception.BadRequestException;
import com.college.sms.exception.DuplicateResourceException;
import com.college.sms.repository.UserRepository;
import com.college.sms.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private MailService mailService;

    private final JwtService jwtService =
            new JwtService("unit-test-secret-unit-test-secret-unit-test-secret", 60_000);

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder,
                authenticationManager, jwtService, mailService);
    }

    private User sampleUser() {
        return User.builder()
                .id(1L).username("admin").email("admin@college.edu")
                .password("hash").role(Role.SUPER_ADMIN).enabled(true)
                .build();
    }

    @Test
    void loginSuccessReturnsTokenAndUser() {
        User user = sampleUser();
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        var principal = new com.college.sms.security.UserPrincipal(user);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        var response = authService.login(new LoginRequest("admin", "Admin@123"));

        assertThat(response.token()).isNotBlank();
        assertThat(response.user().username()).isEqualTo("admin");
        assertThat(response.user().role()).isEqualTo("SUPER_ADMIN");
    }

    @Test
    void loginWrongPasswordThrowsBadRequest() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin", "wrong")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid username or password");
    }

    @Test
    void registerDuplicateUsernameThrowsConflict() {
        when(userRepository.existsByUsernameIgnoreCase("admin")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("admin", "a@b.com", "password123")))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void registerDuplicateEmailThrowsConflict() {
        when(userRepository.existsByUsernameIgnoreCase("newuser")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("a@b.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("newuser", "a@b.com", "password123")))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void registerHashesPasswordAndDefaultsToStudentRole() {
        when(userRepository.existsByUsernameIgnoreCase(anyString())).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var response = authService.register(
                new RegisterRequest("newuser", "new@college.edu", "password123"));

        assertThat(response.message()).contains("successful");
        org.mockito.Mockito.verify(userRepository).save(org.mockito.Mockito.argThat(user ->
                user.getRole() == Role.STUDENT
                        && "encoded".equals(user.getPassword())
                        && user.isEnabled()));
    }
}
