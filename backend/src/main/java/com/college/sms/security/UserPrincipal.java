package com.college.sms.security;

import com.college.sms.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final boolean enabled;
    private final String role;
    private final Long studentId;
    private final Long facultyId;
    private final Long parentId;

    public UserPrincipal(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.enabled = user.isEnabled();
        this.role = user.getRole().name();
        this.studentId = user.getStudentId();
        this.facultyId = user.getFacultyId();
        this.parentId = user.getParentId();
    }

    public Long getStudentId() {
        return studentId;
    }

    public Long getFacultyId() {
        return facultyId;
    }

    public Long getParentId() {
        return parentId;
    }

    public Long getId() {
        return id;
    }

    /** Plain role name, e.g. SUPER_ADMIN — used for JWT claims and API responses. */
    public String getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
