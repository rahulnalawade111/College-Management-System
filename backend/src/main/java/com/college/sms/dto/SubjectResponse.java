package com.college.sms.dto;

public record SubjectResponse(
        Long id,
        String subjectCode,
        String subjectName,
        Integer credits,
        Long semesterId,
        String semesterName,
        Long courseId,
        String courseCode,
        String courseName,
        Long departmentId,
        String departmentName,
        Long facultyId,
        String description) {
}
