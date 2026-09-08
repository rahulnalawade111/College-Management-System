package com.college.sms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record FacultyRequest(
        @NotBlank @Size(max = 30) String employeeId,
        @NotBlank @Size(max = 60) String firstName,
        @NotBlank @Size(max = 60) String lastName,
        @NotBlank @Email @Size(max = 120) String email,
        @Size(max = 20) String phone,
        @Past LocalDate dateOfBirth,
        String gender,
        @Size(max = 120) String qualification,
        Integer experienceYears,
        @Size(max = 80) String designation,
        @NotNull Long departmentId,
        LocalDate joiningDate,
        @Size(max = 255) String photoUrl,
        String status) {
}
