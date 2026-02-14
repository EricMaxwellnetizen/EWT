package com.htc.enter.service;

import com.htc.enter.model.User;

public interface UserAuthService {
    
    User getCurrentUser();
    
    boolean hasAccessLevel(int minLevel);
    
    boolean canAccessManagerResources(Long managerId, int requiredAdminLevel);
    
    /**
     * Checks if a user is a direct report of a manager
     * 
     * @param userId the ID of the potential direct report
     * @param managerId the ID of the manager
     * @return true if user reports directly to the manager
     */
    boolean isDirectReport(Long userId, Long managerId);
}
