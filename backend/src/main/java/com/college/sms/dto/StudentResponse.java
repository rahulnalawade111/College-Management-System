package com.college.sms.dto;

import java.util.List;

public record StudentResponse(
        Long id,
        String studentId,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String phone,
        String dateOfBirth,
        String gender,
        String addressLine1,
        String city,
        String state,
        String pincode,
        String admissionDate,
        Long courseId,
        String courseName,
        Long departmentId,
        String departmentName,
        Long semesterId,
        String semesterName,
        Long academicYearId,
        String academicYearName,
        String status,
        String photoUrl,
        List<GuardianResponse> guardians) {
}
