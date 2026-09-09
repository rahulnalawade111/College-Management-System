package com.college.sms.service;

import com.college.sms.dto.AdminDashboardResponse;
import com.college.sms.dto.FacultyDashboardResponse;
import com.college.sms.dto.ParentDashboardResponse;
import com.college.sms.dto.StudentDashboardResponse;
import com.college.sms.entity.AcademicYear;
import com.college.sms.entity.Course;
import com.college.sms.entity.Department;
import com.college.sms.entity.Enrollment;
import com.college.sms.entity.Faculty;
import com.college.sms.entity.Parent;
import com.college.sms.entity.Semester;
import com.college.sms.entity.Student;
import com.college.sms.repository.AcademicYearRepository;
import com.college.sms.repository.AttendanceRepository;
import com.college.sms.repository.CourseRepository;
import com.college.sms.repository.DepartmentRepository;
import com.college.sms.repository.EnrollmentRepository;
import com.college.sms.repository.FacultyRepository;
import com.college.sms.repository.ParentRepository;
import com.college.sms.repository.StudentRepository;
import com.college.sms.repository.SubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock StudentRepository studentRepository;
    @Mock FacultyRepository facultyRepository;
    @Mock DepartmentRepository departmentRepository;
    @Mock CourseRepository courseRepository;
    @Mock SubjectRepository subjectRepository;
    @Mock EnrollmentRepository enrollmentRepository;
    @Mock AttendanceRepository attendanceRepository;
    @Mock AcademicYearRepository academicYearRepository;
    @Mock ParentRepository parentRepository;

    @InjectMocks DashboardService service;

    private Department csDept;
    private Course bcaCourse;
    private Semester sem1;
    private AcademicYear ay;
    private Student student;

    @BeforeEach
    void setup() {
        csDept = new Department();
        csDept.setId(1L);
        csDept.setDepartmentCode("CS");
        csDept.setDepartmentName("Computer Science");

        bcaCourse = new Course();
        bcaCourse.setId(10L);
        bcaCourse.setCourseCode("BCA");
        bcaCourse.setCourseName("Bachelor of Computer Applications");
        bcaCourse.setDepartment(csDept);

        sem1 = new Semester();
        sem1.setId(20L);
        sem1.setSemesterNumber(1);

        ay = new AcademicYear();
        ay.setId(30L);
        ay.setYearName("2026-27");

        student = new Student();
        student.setId(100L);
        student.setStudentId("BCA2026001");
        student.setFirstName("Ananya");
        student.setLastName("Sharma");
        student.setEmail("ananya@student.example");
        student.setGender("Female");
        student.setCourse(bcaCourse);
        student.setDepartment(csDept);
        student.setSemester(sem1);
        student.setAcademicYear(ay);
        student.setAdmissionDate(LocalDate.now());
        student.setCreatedAt(Instant.now());
    }

    // ------------------------------------------------------------------
    // admin
    // ------------------------------------------------------------------

    @Test
    void adminDashboardAggregatesCountsFromRepositories() {
        when(studentRepository.count()).thenReturn(42L);
        when(facultyRepository.count()).thenReturn(7L);
        when(departmentRepository.count()).thenReturn(4L);
        when(courseRepository.count()).thenReturn(6L);
        when(courseRepository.countByStatus(any())).thenReturn(5L);
        when(departmentRepository.studentCountByDepartment()).thenReturn(List.<Object[]>of(
                new Object[]{1L, "Computer Science", 42L, 20L, 22L}));
        when(attendanceRepository.dailyCountsSince(any())).thenReturn(List.of());
        when(attendanceRepository.lowAttendanceStudents(anyDouble())).thenReturn(List.of());
        when(facultyRepository.findTop3ByCreatedAtGreaterThanEqualOrderByCreatedAtDesc(any()))
                .thenReturn(List.of());
        lenient().when(studentRepository.findTop3ByAdmissionDateGreaterThanEqualOrderByAdmissionDateDesc(any()))
                .thenReturn(List.of());
        when(attendanceRepository.findTop5ByOrderByCreatedAtDesc()).thenReturn(List.of());

        AdminDashboardResponse res = service.adminDashboard();

        assertThat(res.totalStudents()).isEqualTo(42);
        assertThat(res.totalFaculty()).isEqualTo(7);
        assertThat(res.totalDepartments()).isEqualTo(4);
        assertThat(res.totalCourses()).isEqualTo(6);
        assertThat(res.activeCourses()).isEqualTo(5);
        assertThat(res.enrollmentByDepartment()).hasSize(1);
        assertThat(res.enrollmentByDepartment().get(0).departmentName()).isEqualTo("Computer Science");
        assertThat(res.enrollmentByDepartment().get(0).students()).isEqualTo(42);
        // 6 months of admissions data points even with zero admissions
        assertThat(res.admissionsByMonth()).hasSize(6);
        // no attendance today -> null, not 0
        assertThat(res.todayAttendancePercent()).isNull();
    }

    @Test
    void adminTodayAttendanceComputesPresentPlusLatePercent() {
        when(studentRepository.count()).thenReturn(1L);
        when(facultyRepository.count()).thenReturn(0L);
        when(departmentRepository.count()).thenReturn(0L);
        when(courseRepository.count()).thenReturn(0L);
        when(departmentRepository.studentCountByDepartment()).thenReturn(List.of());
        when(attendanceRepository.dailyCountsSince(any())).thenReturn(List.<Object[]>of(
                // date, present, absent, late, excused, total
                new Object[]{LocalDate.now(), 8L, 1L, 1L, 0L, 10L}));
        when(attendanceRepository.lowAttendanceStudents(anyDouble())).thenReturn(List.of());
        when(facultyRepository.findTop3ByCreatedAtGreaterThanEqualOrderByCreatedAtDesc(any()))
                .thenReturn(List.of());
        lenient().when(studentRepository.findTop3ByAdmissionDateGreaterThanEqualOrderByAdmissionDateDesc(any()))
                .thenReturn(List.of());
        when(attendanceRepository.findTop5ByOrderByCreatedAtDesc()).thenReturn(List.of());

        AdminDashboardResponse res = service.adminDashboard();

        // 8 present + 1 late of 10 = 90.0
        assertThat(res.todayAttendancePercent()).isEqualByComparingTo("90.0");
    }

    // ------------------------------------------------------------------
    // faculty
    // ------------------------------------------------------------------

    @Test
    void facultyDashboardComputesOverallPercentAndSubjectLoad() {
        Faculty f = new Faculty();
        f.setId(5L);
        f.setFirstName("Ravi");
        f.setLastName("Patil");
        f.setDesignation("Associate Professor");
        f.setDepartment(csDept);
        when(facultyRepository.findByUserId(800L)).thenReturn(Optional.of(f));

        com.college.sms.entity.Subject subj = new com.college.sms.entity.Subject();
        subj.setId(50L);
        subj.setSubjectCode("CS101");
        subj.setSubjectName("Programming in C");
        subj.setCourse(bcaCourse);
        subj.setSemester(sem1);
        subj.setDepartment(csDept);
        subj.setFacultyId(5L);
        when(subjectRepository.findByFacultyId(5L)).thenReturn(List.of(subj));

        when(enrollmentRepository.countByCourseIdAndSemesterId(10L, 20L)).thenReturn(25L);
        when(attendanceRepository.attendancePercentBySubject(50L)).thenReturn(84.0);
        when(attendanceRepository.overallAttendancePercentByFaculty(5L)).thenReturn(84.0);
        when(attendanceRepository.dailyCountsByFacultySince(eq(5L), any())).thenReturn(List.of());
        when(attendanceRepository.lowAttendanceStudentsByFaculty(eq(5L), anyDouble()))
                .thenReturn(List.of());

        FacultyDashboardResponse res = service.facultyDashboard(800L);

        assertThat(res.name()).isEqualTo("Ravi Patil");
        assertThat(res.totalSubjects()).isEqualTo(1);
        assertThat(res.totalStudents()).isEqualTo(25);
        assertThat(res.overallAttendancePercent()).isEqualTo(84.0);
        assertThat(res.subjectLoad()).hasSize(1);
        assertThat(res.subjectLoad().get(0).subjectName()).isEqualTo("Programming in C");
        assertThat(res.subjectLoad().get(0).students()).isEqualTo(25);
    }

    @Test
    void facultyDashboardWithoutLinkedProfileThrows() {
        when(facultyRepository.findByUserId(anyLong())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.facultyDashboard(1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ------------------------------------------------------------------
    // student
    // ------------------------------------------------------------------

    @Test
    void studentDashboardComputesOverallAndPerSubjectPercent() {
        when(studentRepository.findByUserId(900L)).thenReturn(Optional.of(student));
        // overall: 10 total, 6 present -> 60.0 (attendance % uses PRESENT+LATE from repo)
        when(attendanceRepository.overallByStudentId(100L))
                .thenReturn(List.<Object[]>of(new Object[]{10L, 6L}));
        // subjectSummary: subjectId, name, code, total, present, late, absent, excused
        when(attendanceRepository.subjectSummaryByStudentId(100L)).thenReturn(List.<Object[]>of(
                new Object[]{50L, "Programming in C", "CS101", 10L, 6L, 2L, 2L, 0L}));
        when(attendanceRepository.findTop5ByStudentIdOrderByCreatedAtDesc(100L))
                .thenReturn(List.of());

        StudentDashboardResponse res = service.studentDashboard(900L);

        assertThat(res.name()).isEqualTo("Ananya Sharma");
        assertThat(res.courseName()).isEqualTo("Bachelor of Computer Applications");
        assertThat(res.currentSemesterNumber()).isEqualTo(1);
        assertThat(res.overallAttendancePercent()).isEqualTo(60.0);
        assertThat(res.subjectAttendance()).hasSize(1);
        StudentDashboardResponse.SubjectAttendance sa = res.subjectAttendance().get(0);
        // percent counts present + late = 6 + 2 of 10 = 80.0
        assertThat(sa.percent()).isEqualTo(80.0);
        assertThat(sa.total()).isEqualTo(10);
        assertThat(sa.absent()).isEqualTo(2);
    }

    @Test
    void studentDashboardWithoutLinkedProfileThrows() {
        when(studentRepository.findByUserId(anyLong())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.studentDashboard(1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ------------------------------------------------------------------
    // parent
    // ------------------------------------------------------------------

    @Test
    void parentDashboardReturnsLinkedChildWithAttendance() {
        Parent p = new Parent();
        p.setId(70L);
        p.setStudent(student);
        p.setName("Suresh Sharma");
        p.setRelation("Father");
        when(parentRepository.findByUserIdWithStudent(950L)).thenReturn(List.of(p));
        when(attendanceRepository.overallByStudentId(100L)).thenReturn(List.<Object[]>of(new Object[]{10L, 9L}));
        when(attendanceRepository.subjectSummaryByStudentId(100L)).thenReturn(List.of());
        when(attendanceRepository.findTop5ByStudentIdOrderByCreatedAtDesc(100L))
                .thenReturn(List.of());

        ParentDashboardResponse res = service.parentDashboard(950L);

        assertThat(res.parentName()).isEqualTo("Suresh Sharma");
        assertThat(res.children()).hasSize(1);
        assertThat(res.children().get(0).name()).isEqualTo("Ananya Sharma");
        assertThat(res.children().get(0).overallAttendancePercent()).isEqualTo(90.0);
    }

    @Test
    void parentDashboardWithoutChildrenThrows() {
        when(parentRepository.findByUserIdWithStudent(anyLong())).thenReturn(List.of());
        assertThatThrownBy(() -> service.parentDashboard(1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private static double anyDouble() {
        return org.mockito.ArgumentMatchers.anyDouble();
    }
}
