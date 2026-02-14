package com.htc.enter.controller;

import com.htc.enter.dto.EmailRequest;
import com.htc.enter.dto.EmailResponse;
import com.htc.enter.notification.EmailService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for email sending operations
 * Provides endpoints for authenticated users to send emails
 * 
 * All endpoints require authentication. Some require elevated privileges.
 */
@RestController
@RequestMapping("/api/v1/email")
@Validated
public class EmailController {

    private static final Logger logger = LoggerFactory.getLogger(EmailController.class);
    
    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
        logger.info("✅ Email Controller initialized and ready to handle requests");
    }

    /**
     * Send a simple text email
     * 
     * POST /api/v1/email/send/simple
     * 
     * Request Body:
     * {
     *   "recipientEmail": "user@example.com",
     *   "subject": "Test Email",
     *   "bodyContent": "This is a test email"
     * }
     */
    @PostMapping("/send/simple")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EmailResponse> sendSimpleEmail(@Valid @RequestBody EmailRequest emailRequest) {
        logger.info("📨 Received request to send simple email to: {}", emailRequest.getRecipientEmail());
        
        try {
            emailService.sendEmail(
                emailRequest.getRecipientEmail(),
                emailRequest.getSubject(),
                emailRequest.getBodyContent()
            );
            
            EmailResponse response = EmailResponse.success(
                emailRequest.getRecipientEmail(),
                "Email sent successfully"
            );
            logger.info("✅ Simple email processed successfully for: {}", emailRequest.getRecipientEmail());
            return ResponseEntity.ok(response);
            
        } catch (Exception exception) {
            logger.error("❌ Error processing simple email request: {}", exception.getMessage(), exception);
            EmailResponse errorResponse = EmailResponse.failure(
                emailRequest.getRecipientEmail(),
                "Unexpected error: " + exception.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Send an advanced email with CC, BCC, HTML support
     * 
     * POST /api/v1/email/send/advanced
     * 
     * Request Body:
     * {
     *   "recipientEmail": "user@example.com",
     *   "ccEmails": ["cc1@example.com", "cc2@example.com"],
     *   "bccEmails": ["bcc@example.com"],
     *   "subject": "Advanced Email",
     *   "bodyContent": "<h1>HTML Content</h1>",
     *   "isHtmlContent": true,
     *   "priority": "HIGH"
     * }
     */
    @PostMapping("/send/advanced")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EmailResponse> sendAdvancedEmail(@Valid @RequestBody EmailRequest emailRequest) {
        logger.info("📨 Received request to send advanced email to: {}", emailRequest.getRecipientEmail());
        
        try {
            emailService.sendEmail(
                emailRequest.getRecipientEmail(),
                emailRequest.getSubject(),
                emailRequest.getBodyContent()
            );
            
            EmailResponse response = EmailResponse.success(
                emailRequest.getRecipientEmail(),
                "Advanced email sent successfully"
            );
            logger.info("✅ Advanced email processed successfully for: {}", emailRequest.getRecipientEmail());
            return ResponseEntity.ok(response);
            
        } catch (Exception exception) {
            logger.error("❌ Error processing advanced email request: {}", exception.getMessage(), exception);
            EmailResponse errorResponse = EmailResponse.failure(
                emailRequest.getRecipientEmail(),
                "Unexpected error: " + exception.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Send an HTML email
     * 
     * POST /api/v1/email/send/html
     * 
     * Request Body:
     * {
     *   "recipientEmail": "user@example.com",
     *   "subject": "HTML Email",
     *   "bodyContent": "<html><body><h1>Hello!</h1><p>This is HTML</p></body></html>"
     * }
     */
    @PostMapping("/send/html")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EmailResponse> sendHtmlEmail(@Valid @RequestBody EmailRequest emailRequest) {
        logger.info("📨 Received request to send HTML email to: {}", emailRequest.getRecipientEmail());
        
        try {
            emailService.sendEmail(
                emailRequest.getRecipientEmail(),
                emailRequest.getSubject(),
                emailRequest.getBodyContent()
            );
            
            EmailResponse response = EmailResponse.success(
                emailRequest.getRecipientEmail(),
                "HTML email sent successfully"
            );
            logger.info("✅ HTML email processed successfully for: {}", emailRequest.getRecipientEmail());
            return ResponseEntity.ok(response);
            
        } catch (Exception exception) {
            logger.error("❌ Error processing HTML email request: {}", exception.getMessage(), exception);
            EmailResponse errorResponse = EmailResponse.failure(
                emailRequest.getRecipientEmail(),
                "Unexpected error: " + exception.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Test endpoint to verify email service is working
     * Sends a test email to the specified address
     * Requires ADMIN role
     * 
     * GET /api/v1/email/test?recipientEmail=test@example.com
     */
    @GetMapping("/test")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> testEmailService(
            @RequestParam String recipientEmail) {
        logger.info("🧪 Testing email service with recipient: {}", recipientEmail);
        
        Map<String, Object> testResult = new HashMap<>();
        testResult.put("testedAt", System.currentTimeMillis());
        testResult.put("recipientEmail", recipientEmail);
        
        try {
            String testSubject = "Test Email from Elara";
            String testBody = "This is a test email sent at " + new java.util.Date() + 
                            "\n\nIf you received this, the email service is working correctly!";
            
            emailService.sendEmail(recipientEmail, testSubject, testBody);
            
            testResult.put("success", true);
            testResult.put("message", "Test email sent successfully");
            
            logger.info("✅ Email service test PASSED for: {}", recipientEmail);
            return ResponseEntity.ok(testResult);
            
        } catch (Exception exception) {
            logger.error("❌ Email service test ERROR: {}", exception.getMessage(), exception);
            testResult.put("success", false);
            testResult.put("error", exception.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(testResult);
        }
    }

    /**
     * Health check endpoint for email service
     * Returns the current status of the email service
     * 
     * GET /api/v1/email/health
     */
    @GetMapping("/health")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> checkEmailServiceHealth() {
        logger.debug("🏥 Email service health check requested");
        
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("service", "Email Sending Service");
        healthStatus.put("status", "OPERATIONAL");
        healthStatus.put("timestamp", System.currentTimeMillis());
        healthStatus.put("message", "Email service is ready to send emails");
        
        return ResponseEntity.ok(healthStatus);
    }
}
