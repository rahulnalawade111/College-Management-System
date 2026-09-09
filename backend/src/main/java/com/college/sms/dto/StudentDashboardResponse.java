package com.college.sms.dto;

import java.util.List;

/** Student's own portal summary. */
public record StudentDashboardResponse(
        Long studentId,
        String studentCode,
        String name,
        String courseName,
        String departmentName,
        Integer currentSemesterNumber,
        String academicYearName,
        double overallAttendancePercent,
        List<SubjectAttendance> subjectAttendance,
        long pendingAssignments,
        long upcomingExams,
        double feesPaidPercent,
        Double outstandingFees,
        List<AdminDashboardResponse.UpcomingEvent> upcomingEvents,
        List<AdminDashboardResponse.RecentActivity> recentActivities
) {
    public record SubjectAttendance(
            Long subjectId,
            String subjectCode,
            String subjectName,
            long total,
            long present,
            long absent,
            long late,
            long excused,
            double percent
    ) {}
}
