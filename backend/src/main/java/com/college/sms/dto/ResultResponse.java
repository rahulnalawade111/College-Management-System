package com.college.sms.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ResultResponse(
        Long id,
        Long examId,
        String examName,
        Long studentId,
        String studentCode,
        String studentName,
        Long subjectId,
        String subjectCode,
        String subjectName,
        BigDecimal internalMarks,
        BigDecimal externalMarks,
        BigDecimal practicalMarks,
        BigDecimal totalMarks,
        Integer maxMarks,
        String grade,
        BigDecimal gradePoint,
        Integer credits,
        boolean published,
        Instant publishedAt,
        BigDecimal sgpa,
        BigDecimal cgpa
) {

    /** Student-facing marksheet view. */
    public record Marksheet(
            Long studentId,
            String studentCode,
            String studentName,
            String courseName,
            String examName,
            String academicYearName,
            List<SubjectLine> subjects,
            BigDecimal sgpa,
            BigDecimal cgpa
    ) {}

    public record SubjectLine(
            String subjectCode,
            String subjectName,
            Integer credits,
            BigDecimal internalMarks,
            BigDecimal externalMarks,
            BigDecimal practicalMarks,
            BigDecimal totalMarks,
            Integer maxMarks,
            String grade,
            BigDecimal gradePoint
    ) {}
}
