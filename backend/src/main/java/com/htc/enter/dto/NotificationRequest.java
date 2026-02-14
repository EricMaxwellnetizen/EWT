package com.htc.enter.dto;

/**
 * DTO for notification requests
 */
public class NotificationRequest {
    
    private String recipient;
    private String subject;
    private String message;
    private String priority;
    private String templateId;
    
    public NotificationRequest() {
    }
    
    public NotificationRequest(String recipient, String subject, String message, String priority, String templateId) {
        this.recipient = recipient;
        this.subject = subject;
        this.message = message;
        this.priority = priority;
        this.templateId = templateId;
    }
    
    public static NotificationRequest email(String recipient, String subject, String message) {
        NotificationRequest request = new NotificationRequest();
        request.setRecipient(recipient);
        request.setSubject(subject);
        request.setMessage(message);
        request.setPriority("MEDIUM");
        return request;
    }
    
    // Getters and Setters
    
    public String getRecipient() {
        return recipient;
    }
    
    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public String getTemplateId() {
        return templateId;
    }
    
    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }
}
