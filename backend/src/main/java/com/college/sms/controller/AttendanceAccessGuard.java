package com.college.sms.controller;

import com.college.sms.entity.Parent;
import com.college.sms.exception.ResourceNotFoundException;
import com.college.sms.repository.ParentRepository;
import com.college.sms.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Guards per-student data: a STUDENT may read only their own record, a PARENT only their
 * linked child's. Admins and faculty may read any student.
 */
@Component
public class AttendanceAccessGuard {

    private final ParentRepository parentRepository;

    public AttendanceAccessGuard(ParentRepository parentRepository) {
        this.parentRepository = parentRepository;
    }

    public void requireCanReadStudent(UserPrincipal principal, Long studentId) {
        String role = principal.getRole();
        switch (role) {
            case "SUPER_ADMIN", "ADMIN", "FACULTY" -> { /* full access */ }
            case "STUDENT" -> {
                if (!studentId.equals(principal.getStudentId())) {
                    throw new AccessDeniedException("Students may only view their own attendance");
                }
            }
            case "PARENT" -> {
                Long parentId = principal.getParentId();
                if (parentId == null) {
                    throw new AccessDeniedException("Parent account is not linked to a student");
                }
                Parent parent = parentRepository.findById(parentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Parent record not found"));
                if (!studentId.equals(parent.getStudent().getId())) {
                    throw new AccessDeniedException("Parents may only view their own child's attendance");
                }
            }
            default -> throw new AccessDeniedException("Insufficient permissions");
        }
    }
}
