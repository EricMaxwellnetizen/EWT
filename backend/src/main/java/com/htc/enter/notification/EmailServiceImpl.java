package com.htc.enter.notification;

import java.io.ByteArrayOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import com.htc.enter.notification.*;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Implementation of EmailService for sending emails
 * Uses Spring Boot Mail Starter to send emails via configured SMTP server
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@enterprise.com}")
    private String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        logger.info("✅ EmailServiceImpl initialized");
    }

    /**
     * Send a simple text email
     * 
     * @param to recipient email address
     * @param subject email subject
     * @param body email body
     */
    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            logger.info("✅ Email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("❌ Failed to send email to {}: {}", to, e.getMessage(), e);
        }
    }

    /**
     * Send email with encrypted document attachment
     * 
     * @param to recipient email address
     * @param subject email subject
     * @param body email body
     * @param attachmentName name of the attachment file
     * @param attachmentContent content of the attachment as ByteArrayOutputStream
     */
    @Override
    public void sendEmailWithAttachment(String to, String subject, String body, 
                                        String attachmentName, ByteArrayOutputStream attachmentContent) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            
            if (attachmentContent != null && attachmentName != null) {
                byte[] attachmentBytes = attachmentContent.toByteArray();
                helper.addAttachment(attachmentName, () -> new java.io.ByteArrayInputStream(attachmentBytes));
            }
            
            mailSender.send(mimeMessage);
            logger.info("✅ Email with attachment sent successfully to: {}", to);
        } catch (MessagingException e) {
            logger.error("❌ Failed to send email with attachment to {}: {}", to, e.getMessage(), e);
        } catch (Exception e) {
            logger.error("❌ Unexpected error sending email to {}: {}", to, e.getMessage(), e);
        }
    }
}
