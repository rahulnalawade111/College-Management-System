package com.college.sms.controller;

import com.college.sms.dto.EventRequest;
import com.college.sms.dto.EventResponse;
import com.college.sms.dto.NoticeRequest;
import com.college.sms.dto.NoticeResponse;
import com.college.sms.service.NoticeEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Notices & Events", description = "Admin management of website content")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class NoticeEventController {

    private final NoticeEventService noticeEventService;

    // ---------- Notices ----------

    @GetMapping("/notices")
    @Operation(summary = "List all notices (incl. drafts)")
    public ResponseEntity<List<NoticeResponse>> notices() {
        return ResponseEntity.ok(noticeEventService.findAllNotices());
    }

    @PostMapping("/notices")
    @Operation(summary = "Create a notice")
    public ResponseEntity<NoticeResponse> createNotice(@Valid @RequestBody NoticeRequest request) {
        NoticeResponse created = noticeEventService.createNotice(request);
        return ResponseEntity.created(URI.create("/api/admin/notices/" + created.id())).body(created);
    }

    @PutMapping("/notices/{id}")
    @Operation(summary = "Update a notice")
    public ResponseEntity<NoticeResponse> updateNotice(@PathVariable Long id,
                                                       @Valid @RequestBody NoticeRequest request) {
        return ResponseEntity.ok(noticeEventService.updateNotice(id, request));
    }

    @DeleteMapping("/notices/{id}")
    @Operation(summary = "Delete a notice")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {
        noticeEventService.deleteNotice(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- Events ----------

    @GetMapping("/events")
    @Operation(summary = "List all events (incl. drafts)")
    public ResponseEntity<List<EventResponse>> events() {
        return ResponseEntity.ok(noticeEventService.findAllEvents());
    }

    @PostMapping("/events")
    @Operation(summary = "Create an event")
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest request) {
        EventResponse created = noticeEventService.createEvent(request);
        return ResponseEntity.created(URI.create("/api/admin/events/" + created.id())).body(created);
    }

    @PutMapping("/events/{id}")
    @Operation(summary = "Update an event")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable Long id,
                                                     @Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(noticeEventService.updateEvent(id, request));
    }

    @DeleteMapping("/events/{id}")
    @Operation(summary = "Delete an event")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        noticeEventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
