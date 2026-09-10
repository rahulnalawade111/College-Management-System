package com.college.sms.controller;

import com.college.sms.dto.MessageResponse;
import com.college.sms.dto.UserAdminResponse;
import com.college.sms.entity.User;
import com.college.sms.exception.BadRequestException;
import com.college.sms.exception.ResourceNotFoundException;
import com.college.sms.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin user & role management: list users, enable/disable, reset password.
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Users & Roles", description = "Admin user management")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class UserAdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    @Operation(summary = "List all user accounts")
    public ResponseEntity<List<UserAdminResponse>> findAll() {
        List<UserAdminResponse> users = userRepository.findAll().stream()
                .sorted(java.util.Comparator.comparing(User::getUsername))
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/{id}/enabled")
    @Operation(summary = "Enable or disable a user account")
    public ResponseEntity<UserAdminResponse> setEnabled(@PathVariable Long id,
                                                        @RequestParam boolean enabled) {
        User user = getUser(id);
        user.setEnabled(enabled);
        return ResponseEntity.ok(toResponse(userRepository.save(user)));
    }

    @PostMapping("/{id}/reset-password")
    @Operation(summary = "Admin-reset a user's password to a new value")
    public ResponseEntity<MessageResponse> resetPassword(@PathVariable Long id,
                                                         @RequestParam String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new BadRequestException("New password must be at least 8 characters");
        }
        User user = getUser(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok(MessageResponse.of("Password reset"));
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private UserAdminResponse toResponse(User u) {
        return new UserAdminResponse(u.getId(), u.getUsername(), u.getEmail(),
                u.getRole().name(), u.getStudentId(), u.getFacultyId(), u.isEnabled());
    }
}
