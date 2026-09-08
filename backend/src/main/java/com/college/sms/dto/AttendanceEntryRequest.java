package com.college.sms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AttendanceEntryRequest(
        @NotNull Long studentId,
        @NotBlank String status) {
}
