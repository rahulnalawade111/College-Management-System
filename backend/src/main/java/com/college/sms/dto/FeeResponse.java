package com.college.sms.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record FeeResponse(
        Long id,
        Long studentId,
        String studentCode,
        String studentName,
        String title,
        String feeType,
        BigDecimal amount,
        BigDecimal paidAmount,
        BigDecimal balance,
        LocalDate dueDate,
        String status,
        String paymentMethod,
        Instant paidAt
) {

    public record FeeRequest(
            @NotNull Long studentId,
            @NotBlank @Size(max = 150) String title,
            @NotBlank String feeType,
            @NotNull @DecimalMin("0.01") BigDecimal amount,
            @NotNull LocalDate dueDate
    ) {}

    public record PaymentRequest(
            @NotNull @DecimalMin("0.01") BigDecimal amount,
            String paymentMethod
    ) {}

    public record FeeSummary(
            BigDecimal totalBilled,
            BigDecimal totalCollected,
            BigDecimal totalOutstanding
    ) {}
}
