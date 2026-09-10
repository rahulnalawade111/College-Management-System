package com.college.sms.service;

import com.college.sms.dto.AuthResponse;
import com.college.sms.dto.ForgotPasswordRequest;
import com.college.sms.dto.LoginRequest;
import com.college.sms.dto.MessageResponse;
import com.college.sms.dto.RegisterRequest;
import com.college.sms.dto.ResetPasswordRequest;
import com.college.sms.entity.Role;
import com.college.sms.entity.User;
import com.college.sms.exception.BadRequestException;
import com.college.sms.exception.DuplicateResourceException;
import com.college.sms.exception.ResourceNotFoundException;
import com.college.sms.repository.UserRepository;
import com.college.sms.security.JwtService;
import com.college.sms.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final MailService mailService;

    /** Dev-only in-memory reset tokens (token -> [userId, expiryEpochMs]). */
    private final Map<String, long[]> resetTokens = new ConcurrentHashMap<>();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long RESET_TOKEN_TTL_MS = 30 * 60 * 1000L;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            var principal = (com.college.sms.security.UserPrincipal) authentication.getPrincipal();
            User user = userRepository.findByUsername(principal.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            String token = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole().name());
            return toAuthResponse(token, user);
        } catch (AuthenticationException e) {
            throw new BadRequestException("Invalid username or password");
        }
    }

    @Transactional
    public MessageResponse register(RegisterRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new DuplicateResourceException("Username already taken");
        }
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("Email already registered");
        }
        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.STUDENT) // self-registration defaults to STUDENT
                .enabled(true)
                .build();
        userRepository.save(user);
        return MessageResponse.of("Registration successful. You can now log in.");
    }

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.email()).orElse(null);
        if (user == null) {
            // do not reveal whether the email exists
            return MessageResponse.of("If the email exists, a reset link has been sent");
        }
        String token = newToken();
        resetTokens.put(token, new long[]{user.getId(), Instant.now().toEpochMilli() + RESET_TOKEN_TTL_MS});
        mailService.send(user.getEmail(), "Password reset",
                "Use this token to reset your password: " + token);
        // Dev convenience: token surfaced in response while mail is mocked
        if (mailService.isMock()) {
            return new MessageResponse("Reset token generated (dev mode)", token);
        }
        return MessageResponse.of("If the email exists, a reset link has been sent");
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        long[] entry = resetTokens.remove(request.token());
        if (entry == null || entry[1] < Instant.now().toEpochMilli()) {
            throw new BadRequestException("Invalid or expired reset token");
        }
        User user = userRepository.findById(entry[0])
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        return MessageResponse.of("Password reset successful");
    }

    public AuthResponse me(UserPrincipal principal) {
        User user = userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String token = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole().name());
        return toAuthResponse(token, user);
    }

    @Transactional
    public MessageResponse changePassword(String username, com.college.sms.dto.ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        return MessageResponse.of("Password changed successfully");
    }

    private AuthResponse toAuthResponse(String token, User user) {
        return new AuthResponse(token, "Bearer", jwtService.getExpirationMs(),
                new AuthResponse.UserDTO(user.getId(), user.getUsername(), user.getEmail(),
                        user.getRole().name()));
    }

    private String newToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
