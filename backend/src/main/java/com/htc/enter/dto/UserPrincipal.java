package com.htc.enter.dto;

import java.util.Collection;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.htc.enter.model.User;

public class UserPrincipal implements UserDetails {

    private final User user;

    public UserPrincipal(User userinfo) {
        this.user = userinfo;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Normalize role(s) to uppercase and remove surrounding whitespace.
        // Support comma-separated roles stored in the `role` field (e.g. "admin,manager").
        String rawRole = user.getRole();
        String normalized = (rawRole == null || rawRole.isBlank()) ? "USER" : rawRole.trim().toUpperCase();

        String[] parts = normalized.split("\\s*,\\s*");
        return Arrays.stream(parts)
                .filter(r -> !r.isEmpty())
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        // Must return the hashed password stored in DB
        return user.getPasswordhash();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    // Spring Security checks
    @Override
    public boolean isAccountNonExpired() {
        return true; 
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; 
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // change if you have credential expiry logic
    }

    @Override
    public boolean isEnabled() {
        return true; // change if you have enable/disable logic
    }

    // Optional getter for UserInfo if needed
    public User getUserInfo() {
        return user;
    }
}