package com.college.sms.service;

import com.college.sms.dto.AdminDashboardResponse;
import com.college.sms.dto.FacultyDashboardResponse;
import com.college.sms.dto.ParentDashboardResponse;
import com.college.sms.dto.StudentDashboardResponse;
import com.college.sms.entity.Course;
import com.college.sms.entity.CourseStatus;
import com.college.sms.entity.Department;
import com.college.sms.entity.Enrollment;
import com.college.sms.entity.Faculty;
import com.college.sms.entity.Parent;
import com.college.sms.entity.Student;
import com.college.sms.entity.Subject;
import com.college.sms.repository.AcademicYearRepository;
import com.college.sms.repository.AttendanceRepository;
import com.college.sms.repository.CourseRepository;
import com.college.sms.repository.DepartmentRepository;
import com.college.sms.repository.EnrollmentRepository;
import com.college.sms.repository.FacultyRepository;
import com.college.sms.repository.ParentRepository;
import com.college.sms.repository.StudentRepository;
import com.college.sms.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class DashboardService {

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm");

    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    private final SubjectRepository subjectRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final AcademicYearRepository academicYearRepository;
    private final ParentRepository parentRepository;

    public DashboardService(StudentRepository studentRepository,
                            FacultyRepository facultyRepository,
                            DepartmentRepository departmentRepository,
                            CourseRepository courseRepository,
                            SubjectRepository subjectRepository,
                            EnrollmentRepository enrollmentRepository,
                            AttendanceRepository attendanceRepository,
                            AcademicYearRepository academicYearRepository,
                            ParentRepository parentRepository) {
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.departmentRepository = departmentRepository;
        this.courseRepository = courseRepository;
        this.subjectRepository = subjectRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.attendanceRepository = attendanceRepository;
        this.academicYearRepository = academicYearRepository;
        this.parentRepository = parentRepository;
    }

    // ------------------------------------------------------------------
    // Admin
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public AdminDashboardResponse adminDashboard() {
        LocalDate today = LocalDate.now();

        long totalStudents = studentRepository.count();
        long totalFaculty = facultyRepository.count();
        long totalDepartments = departmentRepository.count();
        long totalCourses = courseRepository.count();
        long activeCourses = courseRepository.countByStatus(CourseStatus.ACTIVE);
        long newAdmissions = studentRepository.countByAdmissionDateBetween(
                today.withDayOfMonth(1), today);

        // Today's attendance % (PRESENT + LATE of all rows saved today)
        BigDecimal todayPercent = attendanceRepository.dailyCountsSince(today).stream()
                .findFirst()
                .map(DashboardService::presentPercentOf)
                .map(p -> BigDecimal.valueOf(p).setScale(1, RoundingMode.HALF_UP))
                .orElse(null);

        List<AdminDashboardResponse.EnrollmentByDepartment> byDept = departmentRepository
                .studentCountByDepartment().stream()
                .map(row -> new AdminDashboardResponse.EnrollmentByDepartment(
                        (Long) row[0],
                        (String) row[1],
                        ((Number) row[2]).longValue(),
                        ((Number) row[3]).longValue(),
                        ((Number) row[4]).longValue()))
                .toList();

        List<AdminDashboardResponse.AdmissionsByMonth> admissions = admissionsByMonth(today);

        List<AdminDashboardResponse.AttendanceTrendPoint> trend = attendanceRepository
                .dailyCountsSince(today.minusDays(13)).stream()
                .map(DashboardService::trendPointOf)
                .toList();

        List<AdminDashboardResponse.LowAttendanceStudent> low = attendanceRepository
                .lowAttendanceStudents(75.0).stream()
                .map(row -> new AdminDashboardResponse.LowAttendanceStudent(
                        (Long) row[0],
                        (String) row[1],
                        ((String) row[2]) + " " + ((String) row[3]),
                        (String) row[4],
                        round1(((Number) row[5]).doubleValue())))
                .toList();

        List<AdminDashboardResponse.RecentActivity> activities = recentAdminActivities();

        return new AdminDashboardResponse(
                totalStudents, totalFaculty, totalDepartments, totalCourses,
                newAdmissions, activeCourses, todayPercent,
                0L, // exams arrive in Phase 4
                byDept, admissions, trend, low, activities, List.of());
    }

    private List<AdminDashboardResponse.AdmissionsByMonth> admissionsByMonth(LocalDate today) {
        // last 6 calendar months including current
        List<AdminDashboardResponse.AdmissionsByMonth> out = new ArrayList<>();
        YearMonth start = YearMonth.from(today).minusMonths(5);
        for (int i = 0; i < 6; i++) {
            YearMonth ym = start.plusMonths(i);
            LocalDate from = ym.atDay(1);
            LocalDate to = ym.atEndOfMonth();
            long count = studentRepository.countByAdmissionDateBetween(from, to);
            out.add(new AdminDashboardResponse.AdmissionsByMonth(
                    ym.atDay(1).format(MONTH_FMT), count));
        }
        return out;
    }

    private List<AdminDashboardResponse.RecentActivity> recentAdminActivities() {
        List<AdminDashboardResponse.RecentActivity> out = new ArrayList<>();
        Instant since = Instant.now().minus(java.time.Duration.ofDays(7));

        // Newest faculty additions
        facultyRepository.findTop3ByCreatedAtGreaterThanEqualOrderByCreatedAtDesc(since)
                .forEach(f -> out.add(activity("faculty", "New faculty joined: "
                        + f.getFirstName() + " " + f.getLastName(),
                        f.getFirstName() + " " + f.getLastName(),
                        fmt(f.getCreatedAt()))));

        // Newest admissions
        studentRepository.findTop3ByAdmissionDateGreaterThanEqualOrderByAdmissionDateDesc(
                        LocalDate.now().minusDays(7))
                .forEach(s -> out.add(activity("admission", "New admission: "
                        + s.getFirstName() + " " + s.getLastName() + " (" + s.getStudentId() + ")",
                        "Admissions", fmt(s.getCreatedAt()))));

        // Latest marked attendance sessions
        attendanceRepository.findTop5ByOrderByCreatedAtDesc().forEach(a -> out.add(activity(
                "attendance",
                "Attendance marked for " + safeSubjectName(a.getSubject().getId()) + " on " + a.getAttendanceDate(),
                "Faculty",
                fmt(a.getCreatedAt()))));

        out.sort((x, y) -> y.time().compareTo(x.time()));
        return out.stream().limit(8).toList();
    }

    private String safeSubjectName(Long subjectId) {
        try {
            return subjectRepository.findById(subjectId)
                    .map(s -> s.getSubjectName() + " (" + s.getSubjectCode() + ")")
                    .orElse("subject #" + subjectId);
        } catch (Exception e) {
            return "subject #" + subjectId;
        }
    }

    // ------------------------------------------------------------------
    // Faculty
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public FacultyDashboardResponse facultyDashboard(Long userId) {
        Faculty faculty = facultyRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("No faculty profile linked to this account"));

        List<Subject> subjects = subjectRepository.findByFacultyId(faculty.getId());
        LocalDate today = LocalDate.now();

        List<FacultyDashboardResponse.SubjectLoad> loads = subjects.stream().map(s -> {
            long students = enrollmentRepository.countByCourseIdAndSemesterId(
                    s.getCourse().getId(), s.getSemester().getId());
            double pct = attendanceRepository.attendancePercentBySubject(s.getId());
            return new FacultyDashboardResponse.SubjectLoad(
                    s.getId(), s.getSubjectCode(), s.getSubjectName(),
                    s.getCourse().getCourseName(), students, round1(pct));
        }).toList();

        long totalStudents = subjects.stream()
                .flatMap(s -> enrollmentRepository
                        .findDistinctStudentIdsByCourseIdAndSemesterId(
                                s.getCourse().getId(), s.getSemester().getId()).stream())
                .distinct().count();
        if (totalStudents == 0) {
            // fall back to enrollment counts when no distinct ids are available
            totalStudents = subjects.stream()
                    .mapToLong(s -> enrollmentRepository.countByCourseIdAndSemesterId(
                            s.getCourse().getId(), s.getSemester().getId()))
                    .sum();
        }

        double overall = attendanceRepository.overallAttendancePercentByFaculty(faculty.getId());

        List<AdminDashboardResponse.AttendanceTrendPoint> trend = attendanceRepository
                .dailyCountsByFacultySince(faculty.getId(), today.minusDays(13)).stream()
                .map(DashboardService::trendPointOf)
                .toList();

        List<AdminDashboardResponse.LowAttendanceStudent> low = attendanceRepository
                .lowAttendanceStudentsByFaculty(faculty.getId(), 75.0).stream()
                .map(row -> new AdminDashboardResponse.LowAttendanceStudent(
                        (Long) row[0], (String) row[1],
                        ((String) row[2]) + " " + ((String) row[3]),
                        (String) row[4], round1(((Number) row[5]).doubleValue())))
                .toList();

        return new FacultyDashboardResponse(
                faculty.getId(),
                faculty.getFirstName() + " " + faculty.getLastName(),
                faculty.getDesignation(),
                faculty.getDepartment().getDepartmentName(),
                subjects.size(),
                totalStudents,
                round1(overall),
                loads, trend, low,
                0L, // pending grading arrives with assignments (Phase 4)
                List.of());
    }

    // ------------------------------------------------------------------
    // Student
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public StudentDashboardResponse studentDashboard(Long userId) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("No student profile linked to this account"));

        List<StudentDashboardResponse.SubjectAttendance> subjects = subjectAttendance(student.getId());

        double overallPercent = overallPercent(student.getId());

        Course course = student.getCourse();

        return new StudentDashboardResponse(
                student.getId(),
                student.getStudentId(),
                student.getFirstName() + " " + student.getLastName(),
                course != null ? course.getCourseName() : null,
                student.getDepartment() != null ? student.getDepartment().getDepartmentName() : null,
                student.getSemester() != null ? student.getSemester().getSemesterNumber() : null,
                student.getAcademicYear() != null ? student.getAcademicYear().getYearName() : null,
                round1(overallPercent),
                subjects,
                0L,  // pending assignments — Phase 4
                0L,   // upcoming exams — Phase 4
                0.0,  // fees arrive in Phase 5
                null,
                List.of(),
                recentStudentActivities(student));
    }

    private List<StudentDashboardResponse.SubjectAttendance> subjectAttendance(Long studentId) {
        return attendanceRepository.subjectSummaryByStudentId(studentId).stream()
                .map(row -> {
                    long total = ((Number) row[3]).longValue();
                    long present = ((Number) row[4]).longValue();
                    long late = ((Number) row[5]).longValue();
                    long absent = ((Number) row[6]).longValue();
                    long excused = ((Number) row[7]).longValue();
                    return new StudentDashboardResponse.SubjectAttendance(
                            (Long) row[0], (String) row[2], (String) row[1],
                            total, present, absent, late, excused,
                            round1(percentOf(total, present + late)));
                })
                .toList();
    }

    private List<AdminDashboardResponse.RecentActivity> recentStudentActivities(Student student) {
        return attendanceRepository
                .findTop5ByStudentIdOrderByCreatedAtDesc(student.getId()).stream()
                .map(a -> activity("attendance",
                        a.getAttendanceDate() + " — marked " + a.getStatus()
                                + " in " + safeSubjectName(a.getSubject().getId()),
                        "Attendance",
                        fmt(a.getCreatedAt())))
                .collect(java.util.stream.Collectors.toList());
    }

    // ------------------------------------------------------------------
    // Parent
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public ParentDashboardResponse parentDashboard(Long userId) {
        List<Parent> links = parentRepository.findByUserIdWithStudent(userId);
        if (links.isEmpty()) {
            throw new IllegalArgumentException("No linked child found for this parent account");
        }

        List<ParentDashboardResponse.ChildSummary> children = links.stream().map(p -> {
            Student s = p.getStudent();
            double pct = overallPercent(s.getId());
            return new ParentDashboardResponse.ChildSummary(
                    s.getId(), s.getStudentId(),
                    s.getFirstName() + " " + s.getLastName(),
                    s.getCourse() != null ? s.getCourse().getCourseName() : null,
                    s.getSemester() != null ? s.getSemester().getSemesterNumber() : null,
                    round1(pct),
                    subjectAttendance(s.getId()),
                    0.0, null, // fees — Phase 5
                    recentStudentActivities(s));
        }).toList();

        Parent first = links.get(0);
        return new ParentDashboardResponse(userId, first.getName(), children);
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    /** Overall attendance % (PRESENT+LATE / total) from the aggregate row. */
    private double overallPercent(Long studentId) {
        List<Object[]> rows = attendanceRepository.overallByStudentId(studentId);
        if (rows.isEmpty() || rows.get(0)[0] == null) {
            return 0.0;
        }
        Object[] r = rows.get(0);
        return percentOf(((Number) r[0]).longValue(), ((Number) r[1]).longValue());
    }

    private static double trendPointHelper(double pct, long sessions) {
        return pct;
    }

    static AdminDashboardResponse.AttendanceTrendPoint trendPointOf(Object[] row) {
        // row: date, present, absent, late, excused, total
        long total = ((Number) row[5]).longValue();
        long present = ((Number) row[1]).longValue();
        long late = ((Number) row[3]).longValue();
        return new AdminDashboardResponse.AttendanceTrendPoint(
                row[0].toString(),
                round1(percentOf(total, present + late)),
                total);
    }

    static double presentPercentOf(Object[] row) {
        long total = ((Number) row[5]).longValue();
        long present = ((Number) row[1]).longValue();
        long late = ((Number) row[3]).longValue();
        return percentOf(total, present + late);
    }

    static double percentOf(long total, long attended) {
        if (total <= 0) {
            return 0.0;
        }
        return attended * 100.0 / total;
    }

    static double round1(double v) {
        return BigDecimal.valueOf(v).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private static AdminDashboardResponse.RecentActivity activity(String type, String message,
                                                                  String actor, String time) {
        return new AdminDashboardResponse.RecentActivity(type, message, actor, time);
    }

    private static String fmt(java.time.Instant instant) {
        return instant == null ? "" : TIME_FMT.format(LocalDateTime.ofInstant(instant,
                java.time.ZoneOffset.UTC)) + " UTC";
    }

}
