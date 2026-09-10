package com.college.sms.service;

import com.college.sms.dto.GuardianRequest;
import com.college.sms.dto.GuardianResponse;
import com.college.sms.dto.StudentRequest;
import com.college.sms.dto.StudentResponse;
import com.college.sms.dto.StudentStatusRequest;
import com.college.sms.entity.AcademicYear;
import com.college.sms.entity.Course;
import com.college.sms.entity.Department;
import com.college.sms.entity.Enrollment;
import com.college.sms.entity.Parent;
import com.college.sms.entity.Role;
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
import com.college.sms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StudentService {

    private static final Set<String> VALID_STATUSES =
            Set.of("ACTIVE", "INACTIVE", "GRADUATED", "SUSPENDED", "TRANSFERRED");

    /** Default password for student login accounts provisioned by the ERP. */
    static final String DEFAULT_STUDENT_PASSWORD = "Student@123";

    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final SemesterRepository semesterRepository;
    private final AcademicYearRepository academicYearRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<StudentResponse> findAll(String search, Long departmentId, Long courseId,
                                         Long semesterId, String status, Pageable pageable) {
        Specification<Student> spec = Specification.where(null);
        if (search != null && !search.isBlank()) {
            String term = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("firstName")), term),
                    cb.like(cb.lower(root.get("lastName")), term),
                    cb.like(cb.lower(root.get("studentId")), term),
                    cb.like(cb.lower(root.get("email")), term)));
        }
        if (departmentId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("department").get("id"), departmentId));
        }
        if (courseId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("course").get("id"), courseId));
        }
        if (semesterId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("semester").get("id"), semesterId));
        }
        if (status != null && !status.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        return studentRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public StudentResponse findById(Long id) {
        return toResponse(getStudent(id));
    }

    @Transactional
    public StudentResponse create(StudentRequest request) {
        validateUnique(null, request.studentId(), request.email());
        validateStatus(request.status());
        Student student = buildStudent(new Student(), request);
        Student saved = studentRepository.save(student);
        if (request.guardians() != null && !request.guardians().isEmpty()) {
            saveGuardians(saved, request.guardians());
        }
        createEnrollment(saved);
        createLoginAccount(saved);
        return toResponse(saved);
    }

    /** Provisions a STUDENT login (username = student ID, default password) for a new student. */
    private void createLoginAccount(Student student) {
        if (userRepository.existsByUsernameIgnoreCase(student.getStudentId())) {
            return;
        }
        User user = User.builder()
                .username(student.getStudentId())
                .email(student.getEmail())
                .password(passwordEncoder.encode(DEFAULT_STUDENT_PASSWORD))
                .role(Role.STUDENT)
                .studentId(student.getId())
                .enabled("ACTIVE".equals(student.getStatus()))
                .build();
        userRepository.save(user);
    }

    @Transactional
    public StudentResponse update(Long id, StudentRequest request) {
        Student student = getStudent(id);
        validateUnique(id, request.studentId(), request.email());
        validateStatus(request.status());
        buildStudent(student, request);
        Student saved = studentRepository.save(student);
        if (request.guardians() != null) {
            parentRepository.deleteByStudentId(saved.getId());
            parentRepository.flush();
            saveGuardians(saved, request.guardians());
        }
        syncEnrollment(saved);
        return toResponse(saved);
    }

    @Transactional
    public StudentResponse updateStatus(Long id, StudentStatusRequest request) {
        Student student = getStudent(id);
        student.setStatus(request.status());
        StudentResponse response = toResponse(studentRepository.save(student));
        // Keep the linked login account in sync: ACTIVE students may log in, others may not.
        userRepository.findByStudentId(id).ifPresent(user -> {
            boolean active = "ACTIVE".equals(request.status());
            if (user.isEnabled() != active) {
                user.setEnabled(active);
                userRepository.save(user);
            }
        });
        return response;
    }

    @Transactional
    public void delete(Long id) {
        Student student = getStudent(id);
        if (enrollmentRepository.existsByStudentId(id)) {
            throw new BadRequestException(
                    "Student has enrollment records. Use status change (e.g. GRADUATED or INACTIVE) instead of delete.");
        }
        parentRepository.deleteByStudentId(id);
        userRepository.findByStudentId(id).ifPresent(user -> {
            user.setEnabled(false);
            userRepository.save(user);
        });
        studentRepository.delete(student);
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private Student buildStudent(Student student, StudentRequest request) {
        student.setStudentId(request.studentId());
        student.setFirstName(request.firstName());
        student.setLastName(request.lastName());
        student.setEmail(request.email());
        student.setPhone(request.phone());
        student.setDateOfBirth(request.dateOfBirth());
        student.setGender(request.gender());
        student.setAddressLine1(request.addressLine1());
        student.setCity(request.city());
        student.setState(request.state());
        student.setPincode(request.pincode());
        student.setAdmissionDate(request.admissionDate());
        student.setCourse(getCourse(request.courseId()));
        student.setDepartment(getDepartment(request.departmentId()));
        student.setSemester(getSemester(request.semesterId()));
        student.setAcademicYear(getAcademicYear(request.academicYearId()));
        student.setPhotoUrl(request.photoUrl());
        if (request.status() != null && !request.status().isBlank()) {
            student.setStatus(request.status());
        } else if (student.getStatus() == null) {
            student.setStatus("ACTIVE");
        }
        return student;
    }

    private void saveGuardians(Student student, List<GuardianRequest> guardians) {
        List<Parent> parents = guardians.stream()
                .map(g -> Parent.builder()
                        .student(student)
                        .name(g.name())
                        .relation(g.relation())
                        .email(g.email())
                        .phone(g.phone())
                        .occupation(g.occupation())
                        .address(g.address())
                        .build())
                .toList();
        parentRepository.saveAll(parents);
    }

    private void createEnrollment(Student student) {
        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(student.getCourse())
                .academicYear(student.getAcademicYear())
                .semester(student.getSemester())
                .enrollmentDate(student.getAdmissionDate() != null
                        ? student.getAdmissionDate() : java.time.LocalDate.now())
                .status("ACTIVE")
                .build();
        enrollmentRepository.save(enrollment);
    }

    private void syncEnrollment(Student student) {
        if (enrollmentRepository.existsByStudentIdAndAcademicYearIdAndSemesterId(
                student.getId(), student.getAcademicYear().getId(), student.getSemester().getId())) {
            return;
        }
        createEnrollment(student);
    }

    private void validateUnique(Long id, String studentId, String email) {
        if (id == null) {
            if (studentRepository.existsByStudentIdIgnoreCase(studentId)) {
                throw new DuplicateResourceException("Student ID already exists: " + studentId);
            }
            if (studentRepository.existsByEmailIgnoreCase(email)) {
                throw new DuplicateResourceException("Student email already exists: " + email);
            }
        } else {
            if (studentRepository.existsByStudentIdIgnoreCaseAndIdNot(studentId, id)) {
                throw new DuplicateResourceException("Student ID already exists: " + studentId);
            }
            if (studentRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
                throw new DuplicateResourceException("Student email already exists: " + email);
            }
        }
    }

    private void validateStatus(String status) {
        if (status != null && !status.isBlank() && !VALID_STATUSES.contains(status)) {
            throw new BadRequestException("Invalid status: " + status);
        }
    }

    private Student getStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + id));
    }

    private Course getCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id " + id));
    }

    private Department getDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id " + id));
    }

    private Semester getSemester(Long id) {
        return semesterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Semester not found with id " + id));
    }

    private AcademicYear getAcademicYear(Long id) {
        return academicYearRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id " + id));
    }

    /** Public mapper used by self-service endpoints (e.g. GET /api/students/me). */
    @Transactional(readOnly = true)
    public StudentResponse toResponseDto(Student s) {
        return toResponse(s);
    }

    private StudentResponse toResponse(Student s) {
        List<GuardianResponse> guardians = parentRepository.findByStudentId(s.getId()).stream()
                .map(p -> new GuardianResponse(p.getId(), p.getName(), p.getRelation(), p.getEmail(),
                        p.getPhone(), p.getOccupation(), p.getAddress()))
                .toList();
        return new StudentResponse(s.getId(), s.getStudentId(), s.getFirstName(), s.getLastName(),
                s.getFirstName() + " " + s.getLastName(), s.getEmail(), s.getPhone(),
                s.getDateOfBirth() == null ? null : s.getDateOfBirth().toString(), s.getGender(),
                s.getAddressLine1(), s.getCity(), s.getState(), s.getPincode(),
                s.getAdmissionDate() == null ? null : s.getAdmissionDate().toString(),
                s.getCourse().getId(), s.getCourse().getCourseName(),
                s.getDepartment().getId(), s.getDepartment().getDepartmentName(),
                s.getSemester().getId(), s.getSemester().getSemesterName(),
                s.getAcademicYear().getId(), s.getAcademicYear().getYearName(),
                s.getStatus(), s.getPhotoUrl(), guardians);
    }
}
