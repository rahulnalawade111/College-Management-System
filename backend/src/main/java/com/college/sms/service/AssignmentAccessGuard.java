package com.college.sms.service;

import com.college.sms.entity.Parent;
import com.college.sms.repository.ParentRepository;
import com.college.sms.repository.StudentRepository;
import com.college.sms.repository.FacultyRepository;
import com.college.sms.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Resolves the current login to its faculty/student profile and guards student-scoped
 * reads (a STUDENT may read only their own record, a PARENT only their linked child).
 */
@Component
public class AssignmentAccessGuard {

    private final ParentRepository parentRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;

    public AssignmentAccessGuard(ParentRepository parentRepository,
                                 StudentRepository studentRepository,
                                 FacultyRepository facultyRepository) {
        this.parentRepository = parentRepository;
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
    }

    private static UserPrincipal principal() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /** The students-row id of the logged-in student (users.student_id → students.id). */
    public Long currentStudentId() {
        Long studentId = principal().getStudentId();
        if (studentId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No student profile linked to this account");
        }
        return studentId;
    }

    /** The faculty-row id of the logged-in faculty (users.faculty_id → faculty.id). */
    public Long currentFacultyId() {
        Long facultyId = principal().getFacultyId();
        if (facultyId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No faculty profile linked to this account");
        }
        return facultyId;
    }

    /** Role-aware access to one student's data. */
    public void guardStudentAccess(Long studentId) {
        String role = principal().getRole();
        switch (role) {
            case "SUPER_ADMIN", "ADMIN", "FACULTY" -> { /* full access */ }
            case "STUDENT" -> {
                if (!studentId.equals(principal().getStudentId())) {
                    throw new AccessDeniedException("Students may only view their own submissions");
                }
            }
            case "PARENT" -> {
                Long parentId = principal().getParentId();
                if (parentId == null) {
                    throw new AccessDeniedException("Parent account is not linked to a student");
                }
                Parent parent = parentRepository.findById(parentId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent record not found"));
                if (!studentId.equals(parent.getStudent().getId())) {
                    throw new AccessDeniedException("Parents may only view their own child's submissions");
                }
            }
            default -> throw new AccessDeniedException("Insufficient permissions");
        }
    }
}
