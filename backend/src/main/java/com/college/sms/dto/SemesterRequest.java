package com.college.sms.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SemesterRequest(
        @NotNull @Min(1) @Max(12) Integer semesterNumber,
        @NotBlank String semesterName,
        @NotNull Long academicYearId) {
}
