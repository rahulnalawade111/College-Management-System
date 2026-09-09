package com.college.sms.service;

import com.college.sms.dto.AssignmentRequest;
import com.college.sms.dto.AssignmentResponse;
import com.college.sms.dto.SubmissionResponse;
import com.college.sms.entity.Assignment;
import com.college.sms.entity.Student;
import com.college.sms.entity.Subject;
import com.college.sms.exception.BadRequestException;
import com.college.sms.exception.ResourceNotFoundException;
import com.college.sms.repository.AssignmentRepository;
import com.college.sms.repository.AssignmentSubmissionRepository;
import com.college.sms.repository.EnrollmentRepository;
import com.college.sms.repository.StudentRepository;
import com.college.sms.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AssignmentService {

    private static final List<String> TYPES = List.of("HOMEWORK", "LAB", "PROJECT", "REPORT");
    private static final List<String> STATUSES = List.of("ACTIVE", "CLOSED");

    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final SubjectRepository subjectRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;

    public AssignmentService(AssignmentRepository assignmentRepository,
                             AssignmentSubmissionRepository submissionRepository,
                             SubjectRepository subjectRepository,
                             EnrollmentRepository enrollmentRepository,
                             StudentRepository studentRepository) {
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.subjectRepository = subjectRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
    }

    // ------------------------------------------------------------------
    // faculty
    // ------------------------------------------------------------------

    @Transactional
    public AssignmentResponse create(AssignmentRequest request, Long facultyUserId) {
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subject not found with id " + request.getSubjectId()));

        if (!TYPES.contains(request.getAssignmentType())) {
            throw new BadRequestException("Invalid assignment type: " + request.getAssignmentType()
                    + ". Allowed: " + TYPES);
        }
        if (request.getMaxMarks() == null || request.getMaxMarks() <= 0) {
            throw new BadRequestException("Max marks must be positive");
        }

        Assignment assignment = Assignment.builder()
                .subject(subject)
                .facultyId(subject.getFacultyId())
                .title(request.getTitle())
                .description(request.getDescription())
                .assignmentType(request.getAssignmentType())
                .maxMarks(request.getMaxMarks())
                .dueDate(request.getDueDate())
                .attachmentUrl(request.getAttachmentUrl())
                .status("ACTIVE")
                .build();
        return toResponse(assignmentRepository.save(assignment));
    }

    @Transactional
    public AssignmentResponse update(Long id, AssignmentRequest request, Long facultyUserId) {
        Assignment assignment = getAssignment(id);
        if (request.getTitle() != null) {
            assignment.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            assignment.setDescription(request.getDescription());
        }
        if (request.getAssignmentType() != null) {
            if (!TYPES.contains(request.getAssignmentType())) {
                throw new BadRequestException("Invalid assignment type: " + request.getAssignmentType());
            }
            assignment.setAssignmentType(request.getAssignmentType());
        }
        if (request.getMaxMarks() != null) {
            if (request.getMaxMarks() <= 0) {
                throw new BadRequestException("Max marks must be positive");
            }
            assignment.setMaxMarks(request.getMaxMarks());
        }
        if (request.getDueDate() != null) {
            assignment.setDueDate(request.getDueDate());
        }
        if (request.getAttachmentUrl() != null) {
            assignment.setAttachmentUrl(request.getAttachmentUrl());
        }
        return toResponse(assignmentRepository.save(assignment));
    }

    @Transactional
    public AssignmentResponse setStatus(Long id, String status) {
        if (!STATUSES.contains(status)) {
            throw new BadRequestException("Invalid status: " + status + ". Allowed: " + STATUSES);
        }
        Assignment assignment = getAssignment(id);
        assignment.setStatus(status);
        return toResponse(assignmentRepository.save(assignment));
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> forFaculty(Long facultyId) {
        return assignmentRepository.findByFacultyIdOrderByCreatedAtDesc(facultyId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> bySubject(Long subjectId) {
        return assignmentRepository.findBySubjectIdOrderByDueDateDesc(subjectId).stream()
                .map(this::toResponse).toList();
    }

    // ------------------------------------------------------------------
    // student
    // ------------------------------------------------------------------

    /** Assignments visible to a student: all ACTIVE ones for their course+semester. */
    @Transactional(readOnly = true)
    public List<AssignmentResponse> forStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + studentId));
        List<Long> subjectIds = subjectRepository
                .findByCourseIdAndSemesterId(student.getCourse().getId(), student.getSemester().getId())
                .stream().map(Subject::getId).toList();
        if (subjectIds.isEmpty()) {
            return List.of();
        }
        return assignmentRepository.findBySubjectIdInOrderByDueDateAsc(subjectIds).stream()
                .map(this::toResponse).toList();
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private Assignment getAssignment(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id " + id));
    }

    private AssignmentResponse toResponse(Assignment a) {
        return new AssignmentResponse(
                a.getId(),
                a.getSubject().getId(),
                a.getSubject().getSubjectCode(),
                a.getSubject().getSubjectName(),
                a.getFacultyId(),
                a.getTitle(),
                a.getDescription(),
                a.getAssignmentType(),
                a.getMaxMarks(),
                a.getDueDate(),
                a.getAttachmentUrl(),
                a.getStatus(),
                a.getCreatedAt(),
                submissionRepository.existsByAssignmentId(a.getId())
                        ? submissionRepository.findByAssignmentIdOrderBySubmittedAtAsc(a.getId()).size() : 0);
    }
}
