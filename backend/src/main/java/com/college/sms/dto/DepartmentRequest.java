package com.college.sms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(
        @NotBlank @Size(max = 20) String departmentCode,
        @NotBlank @Size(max = 120) String departmentName,
        @Size(max = 500) String description,
        Long hodId) {
}
