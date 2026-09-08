package com.college.sms.service;

import com.college.sms.dto.GuardianRequest;
import com.college.sms.dto.StudentRequest;
import com.college.sms.dto.StudentResponse;
import com.college.sms.dto.StudentStatusRequest;
import com.college.sms.entity.AcademicYear;
import com.college.sms.entity.Course;
import com.college.sms.entity.Department;
import com.college.sms.entity.Enrollment;
import com.college.sms.entity.Semester;
import com.college.sms.entity.Student;
import com.college.sms.entity.User;
import com.college.sms.exception.BadRequestException;
import com.college.sms.exception.DuplicateResourceException;
import com.college.sms.exception.ResourceNotFoundException;
import com.college.sms.repository.AcademicYearRepository;
import com.college.sms.repository.CourseRepository;
import com.college.sms.repository.DepartmentRepository;
import com.college.sms.repository.EnrollmentRepository;
import com.college.sms.repository.ParentRepository;
import com.college.sms.repository.SemesterRepository;
import com.college.sms.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock private StudentRepository studentRepository;
    @Mock private ParentRepository parentRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private SemesterRepository semesterRepository;
    @Mock private AcademicYearRepository academicYearRepository;
    @Mock private com.college.sms.repository.UserRepository userRepository;

    private StudentService service;

    private final Department cs = Department.builder().id(1L).departmentCode("CS")
            .departmentName("Computer Science").build();
    private final Course bca = Course.builder().id(1L).courseCode("BCA")
            .courseName("Bachelor of Computer Applications").build();
    private final Semester sem1 = Semester.builder().id(1L).semesterNumber(1)
            .semesterName("Semester 1").build();
    private final AcademicYear year = AcademicYear.builder().id(1L).yearName("2026-2027")
            .active(true).build();

    @BeforeEach
    void setUp() {
        service = new StudentService(studentRepository, parentRepository, enrollmentRepository,
                userRepository, courseRepository, departmentRepository, semesterRepository,
                academicYearRepository,
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder());
    }

    private StudentRequest request(String studentId, String email, List<GuardianRequest> guardians) {
        return new StudentRequest(studentId, "Aarav", "Shah", email, "9811100001",
                LocalDate.of(2006, 2, 10), "MALE", "12 MG Road", "Pune", "MH", "411001",
                LocalDate.of(2026, 6, 15), 1L, 1L, 1L, 1L, null, null, guardians);
    }

    private void stubLookups() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(bca));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(cs));
        when(semesterRepository.findById(1L)).thenReturn(Optional.of(sem1));
        when(academicYearRepository.findById(1L)).thenReturn(Optional.of(year));
    }

    @Test
    void createPersistsStudentGuardiansAndEnrollment() {
        when(studentRepository.existsByStudentIdIgnoreCase("ABC2026CS099")).thenReturn(false);
        when(studentRepository.existsByEmailIgnoreCase("aarav@college.edu")).thenReturn(false);
        stubLookups();
        when(studentRepository.save(any(Student.class)))
                .thenAnswer(inv -> {
                    Student s = inv.getArgument(0);
                    s.setId(42L);
                    return s;
                });
        when(parentRepository.findByStudentId(42L)).thenReturn(List.of());
        when(userRepository.existsByUsernameIgnoreCase("ABC2026CS099")).thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        List<GuardianRequest> guardians = List.of(
                new GuardianRequest("Ramesh Shah", "FATHER", "ramesh@parent.edu", "9822000001", "Service", "12 MG Road"));
        StudentResponse response = service.create(request("ABC2026CS099", "aarav@college.edu", guardians));

        assertThat(response.studentId()).isEqualTo("ABC2026CS099");
        assertThat(response.status()).isEqualTo("ACTIVE");
        verify(parentRepository).saveAll(any());
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    void createRejectsDuplicateStudentId() {
        when(studentRepository.existsByStudentIdIgnoreCase("ABC2026CS001")).thenReturn(true);
        assertThatThrownBy(() -> service.create(request("ABC2026CS001", "new@college.edu", null)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("ABC2026CS001");
    }

    @Test
    void createRejectsInvalidStatus() {
        StudentRequest bad = new StudentRequest("ABC2026CS099", "Aarav", "Shah",
                "aarav9@college.edu", "9811100001",
                LocalDate.of(2006, 2, 10), "MALE", "12 MG Road", "Pune", "MH", "411001",
                LocalDate.of(2026, 6, 15), 1L, 1L, 1L, 1L, "BANNED", null, null);
        assertThatThrownBy(() -> service.create(bad))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void updateReplacesGuardiansWhenProvided() {
        Student existing = Student.builder().id(7L).studentId("ABC2026CS001").firstName("Aarav")
                .lastName("Shah").email("aarav@college.edu").course(bca).department(cs)
                .semester(sem1).academicYear(year).status("ACTIVE").build();
        when(studentRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(studentRepository.existsByStudentIdIgnoreCaseAndIdNot("ABC2026CS001", 7L)).thenReturn(false);
        when(studentRepository.existsByEmailIgnoreCaseAndIdNot("aarav@college.edu", 7L)).thenReturn(false);
        stubLookups();
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));
        when(parentRepository.findByStudentId(7L)).thenReturn(List.of());
        when(enrollmentRepository.existsByStudentIdAndAcademicYearIdAndSemesterId(7L, 1L, 1L))
                .thenReturn(true);

        service.update(7L, request("ABC2026CS001", "aarav@college.edu", List.of()));

        verify(parentRepository).deleteByStudentId(7L);
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void updateStatusDisablesLinkedLoginAccount() {
        Student existing = Student.builder().id(7L).studentId("ABC2026CS001").firstName("Aarav")
                .lastName("Shah").email("aarav@college.edu").course(bca).department(cs)
                .semester(sem1).academicYear(year).status("ACTIVE").build();
        User linked = User.builder().id(20L).username("ABC2026CS001")
                .email("aarav@college.edu").password("x").role(com.college.sms.entity.Role.STUDENT)
                .enabled(true).studentId(7L).build();
        when(studentRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));
        when(parentRepository.findByStudentId(7L)).thenReturn(List.of());
        when(userRepository.findByStudentId(7L)).thenReturn(Optional.of(linked));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        StudentResponse response = service.updateStatus(7L, new StudentStatusRequest("SUSPENDED"));

        assertThat(response.status()).isEqualTo("SUSPENDED");
        assertThat(linked.isEnabled()).isFalse();
    }

    @Test
    void createProvisionsStudentLoginAccount() {
        when(studentRepository.existsByStudentIdIgnoreCase("ABC2026CS099")).thenReturn(false);
        when(studentRepository.existsByEmailIgnoreCase("aarav@college.edu")).thenReturn(false);
        stubLookups();
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> {
            Student s = inv.getArgument(0);
            s.setId(42L);
            return s;
        });
        when(parentRepository.findByStudentId(42L)).thenReturn(List.of());
        when(userRepository.existsByUsernameIgnoreCase("ABC2026CS099")).thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        service.create(request("ABC2026CS099", "aarav@college.edu", null));

        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("ABC2026CS099");
        assertThat(captor.getValue().getRole()).isEqualTo(com.college.sms.entity.Role.STUDENT);
        assertThat(captor.getValue().isEnabled()).isTrue();
    }

    @Test
    void updateStatusChangesStatus() {
        Student existing = Student.builder().id(7L).studentId("ABC2026CS001").firstName("Aarav")
                .lastName("Shah").email("aarav@college.edu").course(bca).department(cs)
                .semester(sem1).academicYear(year).status("ACTIVE").build();
        when(studentRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));
        when(parentRepository.findByStudentId(7L)).thenReturn(List.of());

        StudentResponse response = service.updateStatus(7L, new StudentStatusRequest("SUSPENDED"));

        assertThat(response.status()).isEqualTo("SUSPENDED");
    }

    @Test
    void deleteBlockedWhenEnrollmentsExist() {
        Student existing = Student.builder().id(7L).studentId("ABC2026CS001").firstName("Aarav")
                .lastName("Shah").email("aarav@college.edu").course(bca).department(cs)
                .semester(sem1).academicYear(year).status("ACTIVE").build();
        when(studentRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(enrollmentRepository.existsByStudentId(7L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(7L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("status change");
    }

    @Test
    void findAllFiltersByStatusViaSpecification() {
        Student s = Student.builder().id(7L).studentId("ABC2026CS001").firstName("Aarav")
                .lastName("Shah").email("aarav@college.edu").course(bca).department(cs)
                .semester(sem1).academicYear(year).status("ACTIVE").build();
        when(studentRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class),
                any(Pageable.class))).thenReturn(new PageImpl<>(List.of(s)));
        when(parentRepository.findByStudentId(7L)).thenReturn(List.of());

        var page = service.findAll(null, null, null, null, "ACTIVE", PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).studentId()).isEqualTo("ABC2026CS001");
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
