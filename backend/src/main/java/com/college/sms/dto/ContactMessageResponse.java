package com.college.sms.dto;

import java.time.Instant;

public record ContactMessageResponse(
        Long id,
        String name,
        String email,
        String phone,
        String message,
        String status,
        Instant createdAt) {
}
