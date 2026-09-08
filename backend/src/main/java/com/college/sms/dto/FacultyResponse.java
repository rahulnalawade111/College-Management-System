package com.college.sms.dto;

public record FacultyResponse(
        Long id,
        String employeeId,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String phone,
        String dateOfBirth,
        String gender,
        String qualification,
        Integer experienceYears,
        String designation,
        Long departmentId,
        String departmentName,
        String joiningDate,
        String photoUrl,
        String status) {
}
