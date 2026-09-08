package com.college.sms.dto;

import java.util.List;

public record AttendanceResponse(
        Long id,
        Long studentId,
        String studentName,
        String studentCode,
        Long subjectId,
        String subjectName,
        String facultyId,
        String attendanceDate,
        String status,
        String remarks) {

    public record SubjectSummary(
            Long subjectId,
            String subjectCode,
            String subjectName,
            long total,
            long present,
            long late,
            long absent,
            long excused,
            double percentage) {
    }

    public record OverallSummary(
            long total,
            long present,
            double percentage) {
    }

    public record Page(List<AttendanceResponse> content) {
    }
}
