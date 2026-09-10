package com.college.sms.service;

import com.college.sms.dto.EventRequest;
import com.college.sms.dto.EventResponse;
import com.college.sms.dto.NoticeRequest;
import com.college.sms.dto.NoticeResponse;
import com.college.sms.entity.Event;
import com.college.sms.entity.Notice;
import com.college.sms.exception.ResourceNotFoundException;
import com.college.sms.repository.EventRepository;
import com.college.sms.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeEventService {

    private final NoticeRepository noticeRepository;
    private final EventRepository eventRepository;

    // ---------- Notices ----------

    @Transactional(readOnly = true)
    public List<NoticeResponse> findAllNotices() {
        return noticeRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public NoticeResponse createNotice(NoticeRequest request) {
        Notice notice = Notice.builder()
                .title(request.title())
                .body(request.body())
                .status(request.status().toUpperCase())
                .publishedAt("PUBLISHED".equalsIgnoreCase(request.status()) ? Instant.now() : null)
                .build();
        return toResponse(noticeRepository.save(notice));
    }

    @Transactional
    public NoticeResponse updateNotice(Long id, NoticeRequest request) {
        Notice notice = getNotice(id);
        boolean newlyPublished = "PUBLISHED".equalsIgnoreCase(request.status())
                && !"PUBLISHED".equalsIgnoreCase(notice.getStatus());
        notice.setTitle(request.title());
        notice.setBody(request.body());
        notice.setStatus(request.status().toUpperCase());
        if (newlyPublished) {
            notice.setPublishedAt(Instant.now());
        }
        if (!"PUBLISHED".equalsIgnoreCase(request.status())) {
            notice.setPublishedAt(null);
        }
        return toResponse(noticeRepository.save(notice));
    }

    @Transactional
    public void deleteNotice(Long id) {
        noticeRepository.delete(getNotice(id));
    }

    // ---------- Events ----------

    @Transactional(readOnly = true)
    public List<EventResponse> findAllEvents() {
        return eventRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public EventResponse createEvent(EventRequest request) {
        Event event = Event.builder()
                .title(request.title())
                .description(request.description())
                .eventDate(request.eventDate())
                .venue(request.venue())
                .status(request.status().toUpperCase())
                .build();
        return toResponse(eventRepository.save(event));
    }

    @Transactional
    public EventResponse updateEvent(Long id, EventRequest request) {
        Event event = getEvent(id);
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setEventDate(request.eventDate());
        event.setVenue(request.venue());
        event.setStatus(request.status().toUpperCase());
        return toResponse(eventRepository.save(event));
    }

    @Transactional
    public void deleteEvent(Long id) {
        eventRepository.delete(getEvent(id));
    }

    // ---------- helpers ----------

    private Notice getNotice(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found: " + id));
    }

    private Event getEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + id));
    }

    private NoticeResponse toResponse(Notice n) {
        return new NoticeResponse(n.getId(), n.getTitle(), n.getBody(), n.getStatus(),
                n.getPublishedAt(), n.getCreatedAt());
    }

    private EventResponse toResponse(Event e) {
        return new EventResponse(e.getId(), e.getTitle(), e.getDescription(), e.getEventDate(),
                e.getVenue(), e.getStatus(), e.getCreatedAt());
    }
}
