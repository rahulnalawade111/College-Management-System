package com.college.sms.controller;

import com.college.sms.dto.ExamRequest;
import com.college.sms.dto.ExamResponse;
import com.college.sms.dto.ExamScheduleRequest;
import com.college.sms.service.AssignmentAccessGuard;
import com.college.sms.service.ExamService;
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
@RequestMapping("/api/exams")
public class ExamController {

    private final ExamService examService;
    private final AssignmentAccessGuard accessGuard;

    public ExamController(ExamService examService, AssignmentAccessGuard accessGuard) {
        this.examService = examService;
        this.accessGuard = accessGuard;
    }

    /** Everyone logged in can see the exam list (students use /student/upcoming). */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<ExamResponse> all() {
        return examService.all();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ExamResponse one(@PathVariable Long id) {
        return examService.one(id);
    }

    /** Student-facing: upcoming schedule filtered to their course+semester. */
    @GetMapping("/student/upcoming")
    @PreAuthorize("hasRole('STUDENT')")
    public List<ExamResponse> studentUpcoming() {
        return examService.upcomingForStudentOfCurrentStudent(accessGuard.currentStudentId());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<ExamResponse> create(@Valid @RequestBody ExamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(examService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ExamResponse update(@PathVariable Long id, @Valid @RequestBody ExamRequest request) {
        return examService.update(id, request);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ExamResponse setStatus(@PathVariable Long id, @RequestParam String status) {
        return examService.setStatus(id, status);
    }

    @PostMapping("/{id}/schedules")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FACULTY')")
    public ExamResponse addSchedule(@PathVariable Long id,
                                    @Valid @RequestBody ExamScheduleRequest request) {
        return examService.addSchedule(id, request);
    }

    @DeleteMapping("/{id}/schedules/{scheduleId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ExamResponse removeSchedule(@PathVariable Long id, @PathVariable Long scheduleId) {
        return examService.removeSchedule(id, scheduleId);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("message", "Exam " + id + " deleted"));
    }
}
