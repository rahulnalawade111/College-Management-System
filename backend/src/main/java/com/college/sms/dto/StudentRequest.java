package com.college.sms.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record StudentRequest(
        @NotBlank @Size(max = 30) String studentId,
        @NotBlank @Size(max = 60) String firstName,
        @NotBlank @Size(max = 60) String lastName,
        @NotBlank @Email @Size(max = 120) String email,
        @Size(max = 20) String phone,
        @Past LocalDate dateOfBirth,
        String gender,
        @Size(max = 120) String addressLine1,
        @Size(max = 60) String city,
        @Size(max = 60) String state,
        @Size(max = 10) String pincode,
        LocalDate admissionDate,
        @NotNull Long courseId,
        @NotNull Long departmentId,
        @NotNull Long semesterId,
        @NotNull Long academicYearId,
        String status,
        @Size(max = 255) String photoUrl,
        @Valid List<GuardianRequest> guardians) {
}
