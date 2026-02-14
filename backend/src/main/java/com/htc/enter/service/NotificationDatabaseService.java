package com.htc.enter.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import com.htc.enter.model.Notification;
import com.htc.enter.model.User;
import com.htc.enter.repository.NotificationRepository;
import com.htc.enter.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NotificationDatabaseService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationDatabaseService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a notification for a user
     */
    public Notification createNotification(Long userId, String title, String message, 
                                         Notification.NotificationType type, String entityType, Long entityId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }

        Notification notification = new Notification();
        notification.setUser(user.get());
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRelatedEntityType(entityType);
        notification.setRelatedEntityId(entityId);
        notification.setIsRead(false);

        return notificationRepository.save(notification);
    }

    /**
     * Get recent 5 unread notifications for a user
     */
    public List<Notification> getRecent5UnreadNotifications(Long userId) {
        return notificationRepository.findRecent5Unread(userId);
    }

    /**
     * Get all unread notifications for a user
     */
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    /**
     * Get paginated notifications for a user
     */
    public Page<Notification> getPaginatedNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return notificationRepository.findByUserId(userId, pageable);
    }

    /**
     * Mark notification as read
     */
    public Notification markAsRead(Long notificationId) {
        Optional<Notification> notification = notificationRepository.findById(notificationId);
        if (notification.isEmpty()) {
            throw new IllegalArgumentException("Notification not found with id: " + notificationId);
        }

        Notification n = notification.get();
        n.setIsRead(true);
        return notificationRepository.save(n);
    }

    /**
     * Mark all notifications as read for a user
     */
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        notifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }

    /**
     * Get count of unread notifications
     */
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    /**
     * Delete a notification
     */
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    /**
     * Clean up old notifications (older than 30 days)
     */
    public void cleanupOldNotifications(Long userId) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        notificationRepository.deleteByUserIdAndCreatedAtBefore(userId, cutoffDate);
    }
}
