package com.htc.enter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {
    
    public enum MessageType {
        STORY_UPDATED,
        STORY_CREATED,
        STORY_DELETED,
        PROJECT_UPDATED,
        EPIC_UPDATED,
        NOTIFICATION_CREATED,
        USER_ACTIVITY,
        COMMENT_ADDED
    }
    
    private MessageType type;
    private Long entityId;
    private String entityType; // "Story", "Project", "Epic", etc.
    private String action; // "created", "updated", "deleted"
    private String message;
    private String userName;
    private Long userId;
    private Object payload; // Additional data
    private LocalDateTime timestamp;
    
    public WebSocketMessage(MessageType type, Long entityId, String entityType, String action, String message) {
        this.type = type;
        this.entityId = entityId;
        this.entityType = entityType;
        this.action = action;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
