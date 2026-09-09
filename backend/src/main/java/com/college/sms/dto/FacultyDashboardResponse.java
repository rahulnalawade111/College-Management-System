package com.college.sms.dto;

import java.util.List;

/** Faculty's own teaching overview. */
public record FacultyDashboardResponse(
        Long facultyId,
        String name,
        String designation,
        String departmentName,
        long totalSubjects,
        long totalStudents,
        double overallAttendancePercent,
        List<SubjectLoad> subjectLoad,
        List<AdminDashboardResponse.AttendanceTrendPoint> attendanceTrend,
        List<AdminDashboardResponse.LowAttendanceStudent> lowAttendanceStudents,
        long pendingGrading,
        List<AdminDashboardResponse.RecentActivity> recentActivities
) {
    public record SubjectLoad(
            Long subjectId,
            String subjectCode,
            String subjectName,
            String courseName,
            long students,
            double attendancePercent
    ) {}
}
