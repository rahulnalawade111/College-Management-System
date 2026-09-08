package com.college.sms.service;

/**
 * Mock mail service — in development the reset token is returned in the API
 * response instead of being emailed, so the flow works without SMTP credentials.
 * Swap in a real implementation (JavaMail) for production.
 */
public interface MailService {

    void send(String to, String subject, String body);

    /** true when mail is a no-op mock (frontend may then display the token). */
    default boolean isMock() {
        return true;
    }
}
