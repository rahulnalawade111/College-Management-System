package com.college.sms.dto;

public record SemesterResponse(
        Long id,
        Integer semesterNumber,
        String semesterName,
        Long academicYearId,
        String academicYearName,
        boolean activeYear) {
}
