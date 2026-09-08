package com.college.sms.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record BulkAttendanceRequest(
        @NotNull Long subjectId,
        @NotNull LocalDate attendanceDate,
        String remarks,
        @NotNull List<AttendanceEntryRequest> entries) {
}
