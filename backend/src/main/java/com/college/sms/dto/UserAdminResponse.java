package com.college.sms.dto;

public record UserAdminResponse(
        Long id,
        String username,
        String email,
        String role,
        Long studentId,
        Long facultyId,
        boolean enabled) {}
