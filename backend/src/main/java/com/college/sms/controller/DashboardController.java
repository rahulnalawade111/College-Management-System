package com.college.sms.controller;

import com.college.sms.dto.AdminDashboardResponse;
import com.college.sms.dto.FacultyDashboardResponse;
import com.college.sms.dto.ParentDashboardResponse;
import com.college.sms.dto.StudentDashboardResponse;
import com.college.sms.security.UserPrincipal;
import com.college.sms.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<AdminDashboardResponse> admin() {
        return ResponseEntity.ok(dashboardService.adminDashboard());
    }

    @GetMapping("/faculty")
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<FacultyDashboardResponse> faculty(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(dashboardService.facultyDashboard(principal.getId()));
    }

    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentDashboardResponse> student(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(dashboardService.studentDashboard(principal.getId()));
    }

    @GetMapping("/parent")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ParentDashboardResponse> parent(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(dashboardService.parentDashboard(principal.getId()));
    }
}
