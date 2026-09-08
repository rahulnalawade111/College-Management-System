package com.college.sms.dto;

import java.time.LocalDate;

public record AcademicYearResponse(
        Long id,
        String yearName,
        LocalDate startDate,
        LocalDate endDate,
        boolean active) {
}
