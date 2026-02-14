package com.htc.enter.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.htc.enter.dto.NotificationDTO;
import com.htc.enter.dto.NotificationRequest;
import com.htc.enter.model.Notification;
import com.htc.enter.model.User;
import com.htc.enter.service.NotificationDatabaseService;
import com.htc.enter.util.AccessControlUtil;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationDatabaseService notificationService;
    private final AccessControlUtil accessControlUtil;

    public NotificationController(NotificationDatabaseService notificationService, 
                                 AccessControlUtil accessControlUtil) {
        this.notificationService = notificationService;
        this.accessControlUtil = accessControlUtil;
    }

    /**
     * Create a notification for the current user (used by activity logging)
     * POST /api/v1/notifications/create
     * Body: { "title": "...", "message": "...", "type": "create", "relatedEntityType": "project", "relatedEntityId": 123 }
     */
    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationDTO> createNotification(@Valid @RequestBody NotificationRequest request) {
        User currentUser = accessControlUtil.getCurrentUser();
        
        Notification.NotificationType type = Notification.NotificationType.SYSTEM_ALERT;
        if (request.getSubject() != null) {
            try {
                type = Notification.NotificationType.valueOf(request.getSubject().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Keep default SYSTEM_ALERT type
            }
        }
        
        Notification notification = notificationService.createNotification(
            currentUser.getId(),
            request.getSubject() != null ? request.getSubject() : "Activity",
            request.getMessage(),
            type,
            request.getTemplateId(), // Used as entity type
            request.getPriority() != null ? Long.parseLong(request.getPriority()) : null // Used as entity ID
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(toNotificationDTO(notification));
    }

    /**
     * Create multiple notifications in batch (used by activity logging)
     * POST /api/v1/notifications/create-batch
     * Body: { "notifications": [ {...}, {...} ] }
     */
    @PostMapping("/create-batch")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationDTO>> createNotificationsBatch(@Valid @RequestBody Map<String, List<NotificationRequest>> body) {
        User currentUser = accessControlUtil.getCurrentUser();
        List<NotificationRequest> requests = body.get("notifications");
        
        if (requests == null || requests.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        List<Notification> notifications = requests.stream()
            .map(request -> {
                Notification.NotificationType type = Notification.NotificationType.SYSTEM_ALERT;
                if (request.getSubject() != null) {
                    try {
                        type = Notification.NotificationType.valueOf(request.getSubject().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        // Keep default SYSTEM_ALERT type
                    }
                }
                
                return notificationService.createNotification(
                    currentUser.getId(),
                    request.getSubject() != null ? request.getSubject() : "Activity",
                    request.getMessage(),
                    type,
                    request.getTemplateId(),
                    request.getPriority() != null ? Long.parseLong(request.getPriority()) : null
                );
            })
            .collect(Collectors.toList());
        
        List<NotificationDTO> dtos = notifications.stream()
            .map(this::toNotificationDTO)
            .collect(Collectors.toList());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(dtos);
    }

    /**
     * Get recent 5 unread notifications for current user
     */
    @GetMapping("/recent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationDTO>> getRecentNotifications() {
        User currentUser = accessControlUtil.getCurrentUser();
        List<Notification> notifications = notificationService.getRecent5UnreadNotifications(currentUser.getId());
        
        List<NotificationDTO> dtos = notifications.stream()
            .map(this::toNotificationDTO)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get all unread notifications for current user
     */
    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications() {
        User currentUser = accessControlUtil.getCurrentUser();
        List<Notification> notifications = notificationService.getUnreadNotifications(currentUser.getId());
        
        List<NotificationDTO> dtos = notifications.stream()
            .map(this::toNotificationDTO)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get count of unread notifications
     */
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        User currentUser = accessControlUtil.getCurrentUser();
        long count = notificationService.getUnreadCount(currentUser.getId());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    /**
     * Get paginated notifications for current user
     */
    @GetMapping("/paginated")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<NotificationDTO>> getPaginatedNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User currentUser = accessControlUtil.getCurrentUser();
        Page<Notification> notifications = notificationService.getPaginatedNotifications(currentUser.getId(), page, size);
        
        Page<NotificationDTO> dtos = notifications.map(this::toNotificationDTO);
        return ResponseEntity.ok(dtos);
    }

    /**
     * Mark a notification as read
     */
    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationDTO> markAsRead(@PathVariable Long id) {
        Notification notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(toNotificationDTO(notification));
    }

    /**
     * Mark all notifications as read for current user
     */
    @PutMapping("/mark-all-read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> markAllAsRead() {
        User currentUser = accessControlUtil.getCurrentUser();
        notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }

    /**
     * Delete a notification
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Clean up old notifications
     */
    @DeleteMapping("/cleanup")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, String>> cleanupNotifications(@RequestParam Long userId) {
        notificationService.cleanupOldNotifications(userId);
        return ResponseEntity.ok(Map.of("message", "Old notifications cleaned up"));
    }

    private NotificationDTO toNotificationDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType() != null ? notification.getType().name() : null);
        dto.setIsRead(notification.getIsRead());
        dto.setRelatedEntityType(notification.getRelatedEntityType());
        dto.setRelatedEntityId(notification.getRelatedEntityId());
        dto.setCreatedAt(notification.getCreatedAt());
        if (notification.getUser() != null) {
            dto.setUserId(notification.getUser().getId());
            dto.setUserName(notification.getUser().getUsername());
        }
        return dto;
    }
}
