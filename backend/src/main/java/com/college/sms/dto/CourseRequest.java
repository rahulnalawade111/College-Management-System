package com.college.sms.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CourseRequest(
        @NotBlank @Size(max = 20) String courseCode,
        @NotBlank @Size(max = 120) String courseName,
        @Size(max = 500) String description,
        @NotBlank String duration,
        @NotBlank String degreeType,
        @NotNull Long departmentId,
        @NotNull @Min(1) @Max(12) Integer totalSemesters,
        @NotNull @DecimalMin("0") BigDecimal fees,
        String status) {
}
