package com.college.sms.controller;

import com.college.sms.dto.SubmissionRequest;
import com.college.sms.dto.SubmissionResponse;
import com.college.sms.service.AssignmentAccessGuard;
import com.college.sms.service.SubmissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;
    private final AssignmentAccessGuard accessGuard;

    public SubmissionController(SubmissionService submissionService,
                                AssignmentAccessGuard accessGuard) {
        this.submissionService = submissionService;
        this.accessGuard = accessGuard;
    }

    /** Student submits their own work. LATE is set automatically past the due date. */
    @PostMapping("/assignment/{assignmentId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SubmissionResponse> submit(@PathVariable Long assignmentId,
                                                     @Valid @RequestBody SubmissionRequest request) {
        SubmissionResponse response = submissionService.submit(
                assignmentId, request, accessGuard.currentStudentId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** Student: their own submissions only. */
    @GetMapping("/student/mine")
    @PreAuthorize("hasRole('STUDENT')")
    public List<SubmissionResponse> studentMine() {
        return submissionService.forStudent(accessGuard.currentStudentId());
    }

    /** Faculty/admin: all submissions for one assignment (grading view). */
    @GetMapping("/assignment/{assignmentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FACULTY')")
    public List<SubmissionResponse> forAssignment(@PathVariable Long assignmentId) {
        return submissionService.forAssignment(assignmentId);
    }

    /** Faculty/admin grades a submission. */
    @PutMapping("/{id}/grade")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FACULTY')")
    public SubmissionResponse grade(@PathVariable Long id,
                                    @Valid @RequestBody com.college.sms.dto.GradeRequest request) {
        return submissionService.grade(id, request);
    }

    /** Parent reads their linked child's submissions. */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FACULTY','PARENT')")
    public List<SubmissionResponse> forStudent(@PathVariable Long studentId) {
        accessGuard.guardStudentAccess(studentId);
        return submissionService.forStudent(studentId);
    }
}
