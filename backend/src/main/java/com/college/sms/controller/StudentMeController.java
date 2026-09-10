package com.college.sms.controller;

import com.college.sms.dto.MessageResponse;
import com.college.sms.dto.StudentResponse;
import com.college.sms.entity.Student;
import com.college.sms.exception.BadRequestException;
import com.college.sms.repository.StudentRepository;
import com.college.sms.service.AssignmentAccessGuard;
import com.college.sms.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Student self-service profile: the logged-in student reads their own record.
 */
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Students", description = "Student management and self-service")
public class StudentMeController {

    private final StudentRepository studentRepository;
    private final AssignmentAccessGuard accessGuard;
    private final StudentService studentService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Own student profile (student self-service)")
    public ResponseEntity<StudentResponse> me() {
        Long studentId = accessGuard.currentStudentId();
        Student student = studentRepository.findByIdWithReferences(studentId)
                .orElseThrow(() -> new BadRequestException("Student record not found"));
        return ResponseEntity.ok(studentService.toResponseDto(student));
    }
}
