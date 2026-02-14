package com.htc.enter.service;

import com.htc.enter.dto.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * Broadcast message to all connected clients
     */
    public void broadcastMessage(WebSocketMessage message) {
        try {
            messagingTemplate.convertAndSend("/topic/updates", message);
            log.debug("Broadcasted WebSocket message: {} for entity {}", message.getType(), message.getEntityId());
        } catch (Exception e) {
            log.error("Error broadcasting WebSocket message", e);
        }
    }
    
    /**
     * Send message to specific project subscribers
     */
    public void sendToProject(Long projectId, WebSocketMessage message) {
        try {
            messagingTemplate.convertAndSend("/topic/project/" + projectId, message);
            log.debug("Sent WebSocket message to project {}", projectId);
        } catch (Exception e) {
            log.error("Error sending WebSocket message to project", e);
        }
    }
    
    /**
     * Send message to specific user
     */
    public void sendToUser(String username, WebSocketMessage message) {
        try {
            messagingTemplate.convertAndSendToUser(username, "/queue/notifications", message);
            log.debug("Sent WebSocket message to user {}", username);
        } catch (Exception e) {
            log.error("Error sending WebSocket message to user", e);
        }
    }
    
    /**
     * Notify about story updates
     */
    public void notifyStoryUpdate(Long storyId, Long projectId, String action, String userName) {
        WebSocketMessage message = new WebSocketMessage(
            WebSocketMessage.MessageType.STORY_UPDATED,
            storyId,
            "Story",
            action,
            userName + " " + action + " a story"
        );
        message.setUserName(userName);
        
        // Broadcast to project subscribers
        sendToProject(projectId, message);
        // Also broadcast globally
        broadcastMessage(message);
    }
    
    /**
     * Notify about project updates
     */
    public void notifyProjectUpdate(Long projectId, String action, String userName) {
        WebSocketMessage message = new WebSocketMessage(
            WebSocketMessage.MessageType.PROJECT_UPDATED,
            projectId,
            "Project",
            action,
            userName + " " + action + " the project"
        );
        message.setUserName(userName);
        broadcastMessage(message);
    }
    
    /**
     * Notify about new notifications
     */
    public void notifyNewNotification(Long userId, String username, String notificationMessage) {
        WebSocketMessage message = new WebSocketMessage(
            WebSocketMessage.MessageType.NOTIFICATION_CREATED,
            userId,
            "Notification",
            "created",
            notificationMessage
        );
        sendToUser(username, message);
    }
}
