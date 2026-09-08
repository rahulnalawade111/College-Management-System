package com.college.sms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GuardianRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank String relation,
        @Email @Size(max = 120) String email,
        @Size(max = 20) String phone,
        @Size(max = 80) String occupation,
        @Size(max = 255) String address) {
}
