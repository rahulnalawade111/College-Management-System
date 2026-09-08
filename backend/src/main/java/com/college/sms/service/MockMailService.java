package com.college.sms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MockMailService implements MailService {

    @Override
    public void send(String to, String subject, String body) {
        log.info("[MOCK MAIL] to={} subject={}", to, subject);
    }
}
