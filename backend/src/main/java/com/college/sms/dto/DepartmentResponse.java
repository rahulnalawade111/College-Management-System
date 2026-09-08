package com.college.sms.dto;

public record DepartmentResponse(
        Long id,
        String departmentCode,
        String departmentName,
        String description,
        Long hodId,
        String hodName) {
}
