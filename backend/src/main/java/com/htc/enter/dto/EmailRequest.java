package com.htc.enter.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * Request object for sending emails
 * This DTO ensures all required fields are validated before processing
 */
public class EmailRequest {
    
    @NotBlank(message = "Recipient email address is required")
    @Email(message = "Please provide a valid email address")
    private String recipientEmail;
    
    // Optional: multiple recipients
    private List<String> ccEmails;
    private List<String> bccEmails;
    
    @NotBlank(message = "Email subject is required")
    private String subject;
    
    @NotBlank(message = "Email body content is required")
    private String bodyContent;
    
    // Optional: for HTML emails
    private boolean isHtmlContent = false;
    
    // Optional: priority (HIGH, NORMAL, LOW)
    private String priority = "NORMAL";
    
    public EmailRequest() {
    }
    
    public EmailRequest(String recipientEmail, String subject, String bodyContent) {
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.bodyContent = bodyContent;
    }

    // Getters and Setters with descriptive names
    
    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public List<String> getCcEmails() {
        return ccEmails;
    }

    public void setCcEmails(List<String> ccEmails) {
        this.ccEmails = ccEmails;
    }

    public List<String> getBccEmails() {
        return bccEmails;
    }

    public void setBccEmails(List<String> bccEmails) {
        this.bccEmails = bccEmails;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBodyContent() {
        return bodyContent;
    }

    public void setBodyContent(String bodyContent) {
        this.bodyContent = bodyContent;
    }

    public boolean isHtmlContent() {
        return isHtmlContent;
    }

    public void setHtmlContent(boolean htmlContent) {
        isHtmlContent = htmlContent;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}
