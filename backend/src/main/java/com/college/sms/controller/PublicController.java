package com.college.sms.controller;

import com.college.sms.dto.EventResponse;
import com.college.sms.dto.NoticeResponse;
import com.college.sms.repository.EventRepository;
import com.college.sms.repository.NoticeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Public (unauthenticated) endpoints consumed by the public college website.
 * Only PUBLISHED content is exposed here.
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@Tag(name = "Public Website", description = "Unauthenticated endpoints for the public site")
public class PublicController {

    private final NoticeRepository noticeRepository;
    private final EventRepository eventRepository;

    @GetMapping("/notices")
    @Operation(summary = "Published notices for the public website")
    public ResponseEntity<List<NoticeResponse>> publishedNotices() {
        List<NoticeResponse> notices = noticeRepository
                .findByStatusIgnoreCaseOrderByPublishedAtDesc("PUBLISHED")
                .stream()
                .map(n -> new NoticeResponse(n.getId(), n.getTitle(), n.getBody(), n.getStatus(),
                        n.getPublishedAt(), n.getCreatedAt()))
                .toList();
        return ResponseEntity.ok(notices);
    }

    @GetMapping("/events")
    @Operation(summary = "Published upcoming events for the public website")
    public ResponseEntity<List<EventResponse>> upcomingEvents() {
        List<EventResponse> events = eventRepository
                .findByStatusIgnoreCaseAndEventDateGreaterThanEqualOrderByEventDateAsc("PUBLISHED", LocalDate.now())
                .stream()
                .map(e -> new EventResponse(e.getId(), e.getTitle(), e.getDescription(), e.getEventDate(),
                        e.getVenue(), e.getStatus(), e.getCreatedAt()))
                .toList();
        return ResponseEntity.ok(events);
    }
}
