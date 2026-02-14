package com.htc.enter.serviceimpl;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.htc.enter.model.User;
import com.htc.enter.repository.UserRepository;
import com.htc.enter.service.UserAuthService;

import java.util.Objects;

@Service
public class UserAuthServiceImpl implements UserAuthService {

    private final UserRepository userRepo;

    public UserAuthServiceImpl(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        String username = null;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            username = (String) principal;
        }
        if (username == null) return null;
        return userRepo.findByUsername(username).orElse(null);
    }

    @Override
    public boolean hasAccessLevel(int minLevel) {
        User current = getCurrentUser();
        return current != null && current.getAccessLevel() != null && current.getAccessLevel() >= minLevel;
    }

    @Override
    public boolean canAccessManagerResources(Long managerId, int requiredAdminLevel) {
        User current = getCurrentUser();
        if (current == null) return false;

        return Objects.equals(current.getId(), managerId) || hasAccessLevel(requiredAdminLevel);
    }
    
    @Override
    public boolean isDirectReport(Long userId, Long managerId) {
        if (userId == null || managerId == null) return false;
        
        User user = userRepo.findById(userId).orElse(null);
        if (user == null) return false;
        
        User reportingTo = user.getReportingTo();
        return reportingTo != null && Objects.equals(reportingTo.getId(), managerId);
    }
}