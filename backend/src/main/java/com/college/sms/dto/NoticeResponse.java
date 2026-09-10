package com.college.sms.dto;

import java.time.Instant;

public record NoticeResponse(
        Long id,
        String title,
        String body,
        String status,
        Instant publishedAt,
        Instant createdAt) {
}
