package com.htc.enter.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.htc.enter.dto.UserPrincipal;
import com.htc.enter.exception.ForbiddenException;
import com.htc.enter.model.User;

/**
 * Utility class for role-based access control (RBAC).
 * 
 * Rules:
 * - Regular users can only edit their own details
 * - Admins can edit any user with a lower access level
 * - Managers can edit employees below them in the hierarchy
 */
@Component
public class AccessControlUtil {

    /**
     * Check if the current user has permission to edit the target user.
     * 
     * @param currentUser The current authenticated user
     * @param targetUser The user to be edited
     * @throws ForbiddenException if user does not have permission
     */
    public void checkEditPermission(User currentUser, User targetUser) {
        // Validate inputs
        if (currentUser == null) {
            throw new ForbiddenException("Current user cannot be null");
        }
        if (targetUser == null) {
            throw new ForbiddenException("Target user cannot be null");
        }
        
        // Users can always edit themselves
        if (currentUser.getId().equals(targetUser.getId())) {
            return;
        }

        // Non-admins cannot edit other users
        String currentRole = currentUser.getRole();
        if (currentRole == null || !currentRole.equalsIgnoreCase("ADMIN")) {
            throw new ForbiddenException(
                "You do not have permission to edit user '" + targetUser.getUsername() + "' (id: " + targetUser.getId() + "). " +
                "Only administrators can edit other users' profiles."
            );
        }

        // Admins can edit users with lower access levels
        Integer currentAccessLevel = currentUser.getAccessLevel();
        Integer targetAccessLevel = targetUser.getAccessLevel();

        if (currentAccessLevel == null) {
            throw new ForbiddenException("Your access level is not configured. Please contact administrator.");
        }
        
        if (targetAccessLevel == null) {
            throw new ForbiddenException("Target user's access level is not configured. Cannot determine edit permission.");
        }

        if (targetAccessLevel >= currentAccessLevel) {
            throw new ForbiddenException(
                "Cannot edit user '" + targetUser.getUsername() + "'. " +
                "Administrators can only edit users with lower access levels. " +
                "(Your level: " + currentAccessLevel + ", Their level: " + targetAccessLevel + ")"
            );
        }
    }

    /**
     * Get the current authenticated user from the security context.
     * 
     * @return The current User
     * @throws ForbiddenException if no user is authenticated
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getUserInfo();
        }

        throw new ForbiddenException("Unable to retrieve current user information");
    }

    /**
     * Check if the current user is an admin
     * 
     * @return true if current user is admin, false otherwise
     */
    public boolean isAdmin() {
        try {
            User currentUser = getCurrentUser();
            return currentUser.getRole().equalsIgnoreCase("ADMIN");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the current user has a higher access level than the target user
     * 
     * @param targetUser The user to compare access levels with
     * @return true if current user has higher access level
     */
    public boolean hasHigherAccessLevel(User targetUser) {
        try {
            User currentUser = getCurrentUser();
            Integer currentLevel = currentUser.getAccessLevel();
            Integer targetLevel = targetUser.getAccessLevel();
            
            if (currentLevel == null || targetLevel == null) {
                return false;
            }
            
            return currentLevel > targetLevel;
        } catch (Exception e) {
            return false;
        }
    }
}
