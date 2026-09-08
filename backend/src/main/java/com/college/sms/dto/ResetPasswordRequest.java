package com.college.sms.dto;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @NotBlank String token,
        @NotBlank @jakarta.validation.constraints.Size(min = 6, max = 100) String newPassword) {
}
