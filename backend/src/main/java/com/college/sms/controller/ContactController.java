package com.college.sms.controller;

import com.college.sms.dto.ContactMessageResponse;
import com.college.sms.dto.ContactRequest;
import com.college.sms.dto.MessageResponse;
import com.college.sms.service.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
@Tag(name = "Contact", description = "Public contact form + admin inbox")
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    @Operation(summary = "Submit the public website contact form")
    public ResponseEntity<MessageResponse> submit(@Valid @RequestBody ContactRequest request) {
        contactService.submit(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MessageResponse.of("Thanks for reaching out — our admissions team will reply shortly."));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "List contact messages (admin)")
    public ResponseEntity<List<ContactMessageResponse>> findAll() {
        return ResponseEntity.ok(contactService.findAll());
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Mark a message as read (admin)")
    public ResponseEntity<ContactMessageResponse> markRead(@PathVariable Long id) {
        return ResponseEntity.ok(contactService.markRead(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Delete a message (admin)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contactService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
