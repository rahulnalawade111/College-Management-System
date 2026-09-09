package com.college.sms.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

public record SubmissionResponse(
        Long id,
        Long assignmentId,
        String assignmentTitle,
        Integer maxMarks,
        Long studentId,
        String studentCode,
        String studentName,
        LocalDateTime submittedAt,
        String submissionText,
        String fileUrl,
        String status,
        BigDecimal marksObtained,
        String feedback,
        Instant gradedAt
) {}
