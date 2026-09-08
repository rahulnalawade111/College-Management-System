package com.college.sms.dto;

public record GuardianResponse(
        Long id,
        String name,
        String relation,
        String email,
        String phone,
        String occupation,
        String address) {
}
