package com.college.sms.controller;

import com.college.sms.dto.StudentResponse;
import com.college.sms.entity.Parent;
import com.college.sms.entity.Student;
import com.college.sms.exception.BadRequestException;
import com.college.sms.exception.ResourceNotFoundException;
import com.college.sms.repository.ParentRepository;
import com.college.sms.repository.StudentRepository;
import com.college.sms.security.UserPrincipal;
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

@RestController
@RequestMapping("/api/parent")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Parent", description = "Parent self-service")
public class ParentController {

    private final ParentRepository parentRepository;
    private final StudentRepository studentRepository;

    @GetMapping("/me/child")
    @PreAuthorize("hasRole('PARENT')")
    @Operation(summary = "The linked child's student record (parent self-service)")
    public ResponseEntity<StudentResponse> myChild(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal.getParentId() == null) {
            throw new BadRequestException(
                    "This account is not linked to a student. Contact the administration office.");
        }
        Parent parent = parentRepository.findByIdWithStudent(principal.getParentId())
                .orElseThrow(() -> new ResourceNotFoundException("Parent record not found"));
        Student student = studentRepository.findByIdWithReferences(parent.getStudent().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student record not found"));
        StudentResponse response = new StudentResponse(
                student.getId(), student.getStudentId(), student.getFirstName(),
                student.getLastName(), student.getFirstName() + " " + student.getLastName(),
                student.getEmail(), student.getPhone(),
                student.getDateOfBirth() == null ? null : student.getDateOfBirth().toString(),
                student.getGender(), student.getAddressLine1(), student.getCity(), student.getState(),
                student.getPincode(),
                student.getAdmissionDate() == null ? null : student.getAdmissionDate().toString(),
                student.getCourse().getId(), student.getCourse().getCourseName(),
                student.getDepartment().getId(), student.getDepartment().getDepartmentName(),
                student.getSemester().getId(), student.getSemester().getSemesterName(),
                student.getAcademicYear().getId(), student.getAcademicYear().getYearName(),
                student.getStatus(), student.getPhotoUrl(), null);
        return ResponseEntity.ok(response);
    }
}
