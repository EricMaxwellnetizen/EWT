package com.htc.enter.dto;

/**
 * Response object for email sending operations
 * Provides feedback to the client about email delivery status
 */
public class EmailResponse {
    
    private boolean wasSuccessfullySent;
    private String statusMessage;
    private String recipientEmail;
    private long timestampMillis;
    
    public EmailResponse() {
        this.timestampMillis = System.currentTimeMillis();
    }
    
    public EmailResponse(boolean wasSuccessfullySent, String statusMessage, String recipientEmail) {
        this.wasSuccessfullySent = wasSuccessfullySent;
        this.statusMessage = statusMessage;
        this.recipientEmail = recipientEmail;
        this.timestampMillis = System.currentTimeMillis();
    }
    
    // Factory methods for common scenarios
    
    public static EmailResponse success(String recipientEmail) {
        return new EmailResponse(true, "Email sent successfully", recipientEmail);
    }
    
    public static EmailResponse success(String recipientEmail, String customMessage) {
        return new EmailResponse(true, customMessage, recipientEmail);
    }
    
    public static EmailResponse failure(String recipientEmail, String errorMessage) {
        return new EmailResponse(false, "Failed to send email: " + errorMessage, recipientEmail);
    }
    
    public static EmailResponse queued(String recipientEmail) {
        return new EmailResponse(true, "Email queued for delivery", recipientEmail);
    }

    // Getters and Setters
    
    public boolean isWasSuccessfullySent() {
        return wasSuccessfullySent;
    }

    public void setWasSuccessfullySent(boolean wasSuccessfullySent) {
        this.wasSuccessfullySent = wasSuccessfullySent;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public long getTimestampMillis() {
        return timestampMillis;
    }

    public void setTimestampMillis(long timestampMillis) {
        this.timestampMillis = timestampMillis;
    }
}
