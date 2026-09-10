package com.college.sms.dto;

import java.time.LocalDate;
import java.time.Instant;

public record EventResponse(
        Long id,
        String title,
        String description,
        LocalDate eventDate,
        String venue,
        String status,
        Instant createdAt) {
}
