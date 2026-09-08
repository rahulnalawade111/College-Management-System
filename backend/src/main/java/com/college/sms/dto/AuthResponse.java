package com.college.sms.dto;

import java.util.List;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresInMs,
        UserDTO user) {

    public record UserDTO(Long id, String username, String email, String role) {}
}
