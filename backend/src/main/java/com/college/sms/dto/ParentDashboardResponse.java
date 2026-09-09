package com.college.sms.dto;

import java.util.List;

/** Parent's linked-child summary. */
public record ParentDashboardResponse(
        Long parentUserId,
        String parentName,
        List<ChildSummary> children
) {
    public record ChildSummary(
            Long studentId,
            String studentCode,
            String name,
            String courseName,
            Integer currentSemesterNumber,
            double overallAttendancePercent,
            List<StudentDashboardResponse.SubjectAttendance> subjectAttendance,
            double feesPaidPercent,
            Double outstandingFees,
            List<AdminDashboardResponse.RecentActivity> recentActivities
    ) {}
}
