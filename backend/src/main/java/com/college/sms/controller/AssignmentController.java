package com.college.sms.controller;

import com.college.sms.dto.AssignmentRequest;
import com.college.sms.dto.AssignmentResponse;
import com.college.sms.service.AssignmentAccessGuard;
import com.college.sms.service.AssignmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final AssignmentAccessGuard accessGuard;

    public AssignmentController(AssignmentService assignmentService,
                                AssignmentAccessGuard accessGuard) {
        this.assignmentService = assignmentService;
        this.accessGuard = accessGuard;
    }

    /** Faculty: assignments they created. */
    @GetMapping("/faculty/mine")
    @PreAuthorize("hasRole('FACULTY')")
    public List<AssignmentResponse> mine() {
        return assignmentService.forFaculty(accessGuard.currentFacultyId());
    }

    /** Student: active assignments for their course+semester. */
    @GetMapping("/student/mine")
    @PreAuthorize("hasRole('STUDENT')")
    public List<AssignmentResponse> studentMine() {
        return assignmentService.forStudent(accessGuard.currentStudentId());
    }

    /** Admin: all assignments for one subject. */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public List<AssignmentResponse> bySubject(@RequestParam Long subjectId) {
        return assignmentService.bySubject(subjectId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FACULTY')")
    public ResponseEntity<AssignmentResponse> create(@Valid @RequestBody AssignmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assignmentService.create(request, null));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FACULTY')")
    public AssignmentResponse update(@PathVariable Long id, @Valid @RequestBody AssignmentRequest request) {
        return assignmentService.update(id, request, null);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FACULTY')")
    public AssignmentResponse setStatus(@PathVariable Long id, @RequestParam String status) {
        return assignmentService.setStatus(id, status);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("message", "Assignment " + id + " deleted"));
    }
}
