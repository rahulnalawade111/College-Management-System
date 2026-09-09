package com.college.sms.dto;

import java.time.LocalDate;
import java.util.List;

public record ExamResponse(
        Long id,
        String examName,
        String examType,
        Long academicYearId,
        String academicYearName,
        Long semesterId,
        Integer semesterNumber,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        boolean resultPublished,
        List<ScheduleEntry> schedule
) {
    public record ScheduleEntry(
            Long id,
            Long subjectId,
            String subjectCode,
            String subjectName,
            LocalDate examDate,
            String startTime,
            String endTime,
            String room,
            Integer maxMarks
    ) {}
}
