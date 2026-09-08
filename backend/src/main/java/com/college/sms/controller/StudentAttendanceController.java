package com.college.sms.controller;

import com.college.sms.dto.AttendanceResponse;
import com.college.sms.exception.BadRequestException;
import com.college.sms.security.UserPrincipal;
import com.college.sms.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/student/attendance")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Attendance", description = "Attendance marking and summaries")
public class StudentAttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Own attendance history (student self-service)")
    public ResponseEntity<List<AttendanceResponse>> myAttendance(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(attendanceService.findByStudent(requireStudentId(principal)));
    }

    @GetMapping("/my-summary")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Own per-subject attendance summary (student self-service)")
    public ResponseEntity<List<AttendanceResponse.SubjectSummary>> mySummary(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(attendanceService.subjectSummary(requireStudentId(principal)));
    }

    private Long requireStudentId(UserPrincipal principal) {
        if (principal.getStudentId() == null) {
            throw new BadRequestException(
                    "This account is not linked to a student record. Contact the administration office.");
        }
        return principal.getStudentId();
    }
}
