package com.college.sms.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** One row of the marks-entry grid: internal + external + practical for a student+subject. */
public record MarksEntryRequest(
        @NotNull Long studentId,
        @NotNull Long subjectId,
        @DecimalMin("0") BigDecimal internalMarks,
        @DecimalMin("0") BigDecimal externalMarks,
        @DecimalMin("0") BigDecimal practicalMarks,
        Integer credits
) {}
