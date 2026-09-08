package com.college.sms.dto;

import java.math.BigDecimal;

public record CourseResponse(
        Long id,
        String courseCode,
        String courseName,
        String description,
        String duration,
        String degreeType,
        Long departmentId,
        String departmentCode,
        String departmentName,
        Integer totalSemesters,
        BigDecimal fees,
        String status) {
}
