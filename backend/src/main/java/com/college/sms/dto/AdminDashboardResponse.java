package com.college.sms.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Aggregated admin ERP overview. */
public record AdminDashboardResponse(
        long totalStudents,
        long totalFaculty,
        long totalDepartments,
        long totalCourses,
        long newAdmissionsThisMonth,
        long activeCourses,
        BigDecimal todayAttendancePercent,
        long upcomingExamsCount,
        List<EnrollmentByDepartment> enrollmentByDepartment,
        List<AdmissionsByMonth> admissionsByMonth,
        List<AttendanceTrendPoint> attendanceTrend,
        List<LowAttendanceStudent> lowAttendanceStudents,
        List<RecentActivity> recentActivities,
        List<UpcomingEvent> upcomingEvents
) {
    public record EnrollmentByDepartment(
            Long departmentId,
            String departmentName,
            long students,
            long maleStudents,
            long femaleStudents
    ) {}

    public record AdmissionsByMonth(String month, long admissions) {}

    public record AttendanceTrendPoint(String date, double presentPercent, long sessions) {}

    public record LowAttendanceStudent(
            Long studentId,
            String studentCode,
            String name,
            String courseName,
            double attendancePercent
    ) {}

    public record RecentActivity(
            String type,
            String message,
            String actor,
            String time
    ) {}

    public record UpcomingEvent(
            Long id,
            String title,
            LocalDate date,
            String location
    ) {}

    public record ExamCount(long upcomingExams) {}
}
