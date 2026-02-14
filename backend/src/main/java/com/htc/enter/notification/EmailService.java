package com.htc.enter.notification;

import java.io.ByteArrayOutputStream;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
    
    /**
     * Send email with encrypted document attachment
     * 
     * @param to recipient email address
     * @param subject email subject
     * @param body email body
     * @param attachmentName name of the attachment file
     * @param attachmentContent content of the attachment as ByteArrayOutputStream
     */
    void sendEmailWithAttachment(String to, String subject, String body, 
                                  String attachmentName, ByteArrayOutputStream attachmentContent);
}
