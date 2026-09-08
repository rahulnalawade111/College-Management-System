package com.college.sms.dto;

public record ForgotPasswordRequest(
        @jakarta.validation.constraints.Email @jakarta.validation.constraints.NotBlank String email) {
}
