package com.college.sms.controller;

import com.college.sms.dto.MarksEntryRequest;
import com.college.sms.dto.ResultResponse;
import com.college.sms.service.AssignmentAccessGuard;
import com.college.sms.service.ResultService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/results")
public class ResultController {

    private final ResultService resultService;
    private final AssignmentAccessGuard accessGuard;

    public ResultController(ResultService resultService, AssignmentAccessGuard accessGuard) {
        this.resultService = resultService;
        this.accessGuard = accessGuard;
    }

    /** Marks entry (bulk per subject allowed). ADMIN/FACULTY only — students get 403. */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FACULTY')")
    public List<ResultResponse> enter(@RequestParam Long examId,
                                      @Valid @RequestBody List<MarksEntryRequest> entries) {
        return resultService.enterMarks(examId, entries);
    }

    /** Publish all results of an exam — locks them. ADMIN/SUPER_ADMIN only. */
    @PostMapping("/{examId}/publish")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Map<String, Object>> publish(@PathVariable Long examId) {
        int count = resultService.publishExam(examId);
        return ResponseEntity.ok(Map.of("message", "Published and locked " + count + " results", "count", count));
    }

    /** SUPER_ADMIN unlock. */
    @PostMapping("/{examId}/unpublish")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> unpublish(@PathVariable Long examId) {
        int count = resultService.unpublishExam(examId);
        return ResponseEntity.ok(Map.of("message", "Unlocked " + count + " results", "count", count));
    }

    /** ADMIN/FACULTY: all results of an exam, optionally filtered to one student. */
    @GetMapping("/exam/{examId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FACULTY')")
    public List<ResultResponse> forExam(@PathVariable Long examId,
                                        @org.springframework.web.bind.annotation.RequestParam(required = false) Long studentId) {
        if (studentId != null) {
            return resultService.forExamStudent(examId, studentId);
        }
        return resultService.forExam(examId);
    }

    /** ADMIN/FACULTY: student's results in an exam (parents use the marksheet endpoint with guard). */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FACULTY','PARENT')")
    public List<ResultResponse> forStudent(@PathVariable Long studentId,
                                           @org.springframework.web.bind.annotation.RequestParam Long examId) {
        accessGuard.guardStudentAccess(studentId);
        return resultService.forExamStudent(examId, studentId);
    }

    /** Student's own published results. */
    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public List<ResultResponse> me(
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long examId) {
        return resultService.forStudentPublished(accessGuard.currentStudentId(), examId);
    }

    /** Marksheet (student's own). */
    @GetMapping("/me/marksheet")
    @PreAuthorize("hasRole('STUDENT')")
    public ResultResponse.Marksheet myMarksheet(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "1") Long examId) {
        return resultService.marksheet(examId, accessGuard.currentStudentId());
    }

    /** Marksheet for a specific student — parent must be linked. */
    @GetMapping("/student/{studentId}/marksheet")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FACULTY','PARENT')")
    public ResultResponse.Marksheet marksheet(
            @PathVariable Long studentId,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "1") Long examId) {
        accessGuard.guardStudentAccess(studentId);
        return resultService.marksheet(examId, studentId);
    }
}
