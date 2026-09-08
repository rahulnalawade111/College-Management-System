package com.college.sms.dto;

public record MessageResponse(String message, String resetToken) {

    public static MessageResponse of(String message) {
        return new MessageResponse(message, null);
    }
}
