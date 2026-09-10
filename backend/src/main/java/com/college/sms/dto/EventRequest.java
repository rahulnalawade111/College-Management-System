package com.college.sms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EventRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 5000) String description,
        @NotNull LocalDate eventDate,
        @Size(max = 200) String venue,
        @NotBlank @Size(max = 20) String status) {
}
