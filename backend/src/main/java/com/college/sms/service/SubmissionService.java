package com.college.sms.service;

import com.college.sms.dto.GradeRequest;
import com.college.sms.dto.SubmissionRequest;
import com.college.sms.dto.SubmissionResponse;
import com.college.sms.entity.Assignment;
import com.college.sms.entity.AssignmentSubmission;
import com.college.sms.entity.Student;
import com.college.sms.exception.BadRequestException;
import com.college.sms.exception.DuplicateResourceException;
import com.college.sms.exception.ResourceNotFoundException;
import com.college.sms.repository.AssignmentRepository;
import com.college.sms.repository.AssignmentSubmissionRepository;
import com.college.sms.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubmissionService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final StudentRepository studentRepository;

    public SubmissionService(AssignmentRepository assignmentRepository,
                             AssignmentSubmissionRepository submissionRepository,
                             StudentRepository studentRepository) {
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.studentRepository = studentRepository;
    }

    /** Student submits an assignment. After the due date the status is LATE automatically. */
    @Transactional
    public SubmissionResponse submit(Long assignmentId, SubmissionRequest request, Long studentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id " + assignmentId));

        if (!"ACTIVE".equals(assignment.getStatus())) {
            throw new BadRequestException("This assignment is closed and no longer accepts submissions");
        }
        if ((request.getSubmissionText() == null || request.getSubmissionText().isBlank())
                && (request.getFileUrl() == null || request.getFileUrl().isBlank())) {
            throw new BadRequestException("Provide submission text or a file to submit");
        }

        if (submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId).isPresent()) {
            throw new DuplicateResourceException(
                    "You have already submitted this assignment. Edit your existing submission instead.");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + studentId));

        LocalDateTime now = LocalDateTime.now();
        boolean late = assignment.getDueDate() != null && now.isAfter(assignment.getDueDate());

        AssignmentSubmission submission = AssignmentSubmission.builder()
                .assignment(assignment)
                .student(student)
                .submittedAt(now)
                .submissionText(request.getSubmissionText())
                .fileUrl(request.getFileUrl())
                .status(late ? "LATE" : "SUBMITTED")
                .build();
        return toResponse(submissionRepository.save(submission));
    }

    /** Faculty grades a submission. Marks must be within the assignment's max. */
    @Transactional
    public SubmissionResponse grade(Long submissionId, GradeRequest request) {
        AssignmentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id " + submissionId));

        BigDecimal marks = request.getMarksObtained();
        int max = submission.getAssignment().getMaxMarks();
        if (marks.compareTo(BigDecimal.ZERO) < 0 || marks.compareTo(BigDecimal.valueOf(max)) > 0) {
            throw new BadRequestException("Marks must be between 0 and " + max);
        }

        submission.setMarksObtained(marks);
        submission.setFeedback(request.getFeedback());
        submission.setStatus("GRADED");
        submission.setGradedAt(java.time.Instant.now());
        return toResponse(submissionRepository.save(submission));
    }

    // ------------------------------------------------------------------
    // reads
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<SubmissionResponse> forAssignment(Long assignmentId) {
        if (!assignmentRepository.existsById(assignmentId)) {
            throw new ResourceNotFoundException("Assignment not found with id " + assignmentId);
        }
        return submissionRepository.findByAssignmentIdOrderBySubmittedAtAsc(assignmentId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> forStudent(Long studentId) {
        return submissionRepository.findByStudentIdOrderBySubmittedAtDesc(studentId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SubmissionResponse findOne(Long submissionId) {
        return toResponse(submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id " + submissionId)));
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private SubmissionResponse toResponse(AssignmentSubmission s) {
        return new SubmissionResponse(
                s.getId(),
                s.getAssignment().getId(),
                s.getAssignment().getTitle(),
                s.getAssignment().getMaxMarks(),
                s.getStudent().getId(),
                s.getStudent().getStudentId(),
                s.getStudent().getFirstName() + " " + s.getStudent().getLastName(),
                s.getSubmittedAt(),
                s.getSubmissionText(),
                s.getFileUrl(),
                s.getStatus(),
                s.getMarksObtained(),
                s.getFeedback(),
                s.getGradedAt());
    }
}
