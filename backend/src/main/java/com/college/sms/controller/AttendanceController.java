package com.college.sms.controller;

import com.college.sms.dto.AttendanceResponse;
import com.college.sms.dto.BulkAttendanceRequest;
import com.college.sms.security.UserPrincipal;
import com.college.sms.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Attendance", description = "Attendance marking and summaries")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AttendanceAccessGuard accessGuard;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FACULTY')")
    @Operation(summary = "Bulk-mark attendance for a subject and date")
    public ResponseEntity<List<AttendanceResponse>> bulkMark(
            @Valid @RequestBody BulkAttendanceRequest request) {
        return ResponseEntity.ok(attendanceService.bulkMark(request));
    }

    @GetMapping("/subject/{subjectId}/date/{date}")
    @Operation(summary = "Attendance saved for a subject on a date (pre-fill for re-opened form)")
    public ResponseEntity<List<AttendanceResponse>> findBySubjectAndDate(
            @PathVariable Long subjectId, @PathVariable LocalDate date) {
        return ResponseEntity.ok(attendanceService.findBySubjectAndDate(subjectId, date));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FACULTY','STUDENT','PARENT')")
    @Operation(summary = "Full attendance history for a student (self/own-child or staff)")
    public ResponseEntity<List<AttendanceResponse>> findByStudent(
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        accessGuard.requireCanReadStudent(principal, studentId);
        return ResponseEntity.ok(attendanceService.findByStudent(studentId));
    }

    @GetMapping("/student/{studentId}/summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FACULTY','STUDENT','PARENT')")
    @Operation(summary = "Per-subject attendance summary for a student (self/own-child or staff)")
    public ResponseEntity<List<AttendanceResponse.SubjectSummary>> subjectSummary(
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        accessGuard.requireCanReadStudent(principal, studentId);
        return ResponseEntity.ok(attendanceService.subjectSummary(studentId));
    }

    @GetMapping("/student/{studentId}/overall")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FACULTY','STUDENT','PARENT')")
    @Operation(summary = "Overall attendance percentage for a student (self/own-child or staff)")
    public ResponseEntity<AttendanceResponse.OverallSummary> overallSummary(
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        accessGuard.requireCanReadStudent(principal, studentId);
        return ResponseEntity.ok(attendanceService.overallSummary(studentId));
    }
}
