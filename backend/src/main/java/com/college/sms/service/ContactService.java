package com.college.sms.service;

import com.college.sms.dto.ContactMessageResponse;
import com.college.sms.dto.ContactRequest;
import com.college.sms.entity.ContactMessage;
import com.college.sms.exception.ResourceNotFoundException;
import com.college.sms.repository.ContactMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactService {

    private static final String CONTACT_INBOX = "admissions@abccollege.edu";

    private final ContactMessageRepository contactMessageRepository;
    private final MailService mailService;

    @Transactional
    public void submit(ContactRequest request) {
        ContactMessage saved = contactMessageRepository.save(ContactMessage.builder()
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .message(request.message())
                .status("NEW")
                .build());
        mailService.send(CONTACT_INBOX,
                "Website contact form: " + request.name(),
                "From: " + request.name() + " <" + request.email() + ">\n\n" + request.message());
    }

    @Transactional(readOnly = true)
    public List<ContactMessageResponse> findAll() {
        return contactMessageRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public ContactMessageResponse markRead(Long id) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found: " + id));
        message.setStatus("READ");
        return toResponse(contactMessageRepository.save(message));
    }

    @Transactional
    public void delete(Long id) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found: " + id));
        contactMessageRepository.delete(message);
    }

    private ContactMessageResponse toResponse(ContactMessage m) {
        return new ContactMessageResponse(m.getId(), m.getName(), m.getEmail(), m.getPhone(),
                m.getMessage(), m.getStatus(), m.getCreatedAt());
    }
}
