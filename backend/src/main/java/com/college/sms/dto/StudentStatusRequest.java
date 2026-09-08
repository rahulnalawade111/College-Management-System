package com.college.sms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record StudentStatusRequest(
        @NotBlank
        @Pattern(regexp = "ACTIVE|INACTIVE|GRADUATED|SUSPENDED|TRANSFERRED",
                 message = "Status must be one of ACTIVE, INACTIVE, GRADUATED, SUSPENDED, TRANSFERRED")
        String status) {
}
