package com.htc.enter.service;

import java.io.ByteArrayOutputStream;

import com.htc.enter.model.Project;

public interface ProjectNotificationService {
    
    void notifyManagerOnProjectCreation(Project project);
    
    void sendEmailWithAttachment(String to, String subject, String body, 
                                  String attachmentName, ByteArrayOutputStream attachmentContent);
}
