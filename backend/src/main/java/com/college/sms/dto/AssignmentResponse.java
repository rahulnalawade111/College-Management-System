package com.college.sms.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

public record AssignmentResponse(
        Long id,
        Long subjectId,
        String subjectCode,
        String subjectName,
        Long facultyId,
        String title,
        String description,
        String assignmentType,
        Integer maxMarks,
        LocalDateTime dueDate,
        String attachmentUrl,
        String status,
        Instant createdAt,
        long submissionCount
) {}
