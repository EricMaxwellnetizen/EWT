package com.htc.enter.dto;

/**
 * DTO for notification responses from external services
 */
public class NotificationResponse {
    
    private boolean success;
    private String message;
    private String notificationId;
    private String status;

    public NotificationResponse() {
    }

    public NotificationResponse(boolean success, String message, String notificationId, String status) {
        this.success = success;
        this.message = message;
        this.notificationId = notificationId;
        this.status = status;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
