package com.college.sms.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubjectRequest(
        @NotBlank @Size(max = 20) String subjectCode,
        @NotBlank @Size(max = 120) String subjectName,
        @NotNull @Min(1) @Max(10) Integer credits,
        @NotNull Long semesterId,
        @NotNull Long courseId,
        @NotNull Long departmentId,
        Long facultyId,
        @Size(max = 500) String description) {
}
