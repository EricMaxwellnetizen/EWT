package com.htc.enter.notification;

import org.springframework.stereotype.Service;

import com.htc.enter.model.Story;
import com.htc.enter.model.Epic;

@Service
public class NotificationService {

    private final EmailService emailService;

    public NotificationService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void notifyStoryAssigned(Story story) {
        if (story.getAssigned_to() == null || story.getAssigned_to().getEmail() == null) return;
        String to = story.getAssigned_to().getEmail();
        String subject = "New Task Assigned: " + story.getTitle();
        String body = "Hello " + story.getAssigned_to().getUsername() + ",\n\nYou have been assigned a new task: '" + story.getTitle() + "' in project '" +
                (story.getProjectId() != null ? story.getProjectId().getName() : "") + "'.\nDue date: " + story.getDueDate() + "\n\nDescription:\n" + story.getDeliverables();
        emailService.sendEmail(to, subject, body);
    }

    public void notifyStoryCompleted(Story story) {
        if (story.getManager() == null || story.getManager().getEmail() == null) return;
        String to = story.getManager().getEmail();
        String subject = "Task Completed: " + story.getTitle();
        String body = "Hello " + story.getManager().getUsername() + ",\n\nThe task '" + story.getTitle() + "' has been marked completed/approved.\n\nRegards";
        emailService.sendEmail(to, subject, body);
    }

    public void notifyEpicApproved(Epic epic) {
        if (epic.getProjectId() == null) return;
        // notify project manager(s) or createdBy - here we try to use project's manager email if available
        if (epic.getProjectId().getManager_id() != null && epic.getProjectId().getManager_id().getEmail() != null) {
            String to = epic.getProjectId().getManager_id().getEmail();
            String subject = "Epic Approved: " + epic.getName();
            String body = "Epic '" + epic.getName() + "' has been approved.";
            emailService.sendEmail(to, subject, body);
        }
    }

    public void notifyEpicFinished(Epic epic) {
        if (epic.getProjectId() == null) return;
        if (epic.getProjectId().getManager_id() != null && epic.getProjectId().getManager_id().getEmail() != null) {
            String to = epic.getProjectId().getManager_id().getEmail();
            String subject = "Epic Finished: " + epic.getName();
            String body = "Epic '" + epic.getName() + "' has been finished.";
            emailService.sendEmail(to, subject, body);
        }
    }

    public void notifyOverdue(Story story) {
        if (story.getAssigned_to() == null || story.getAssigned_to().getEmail() == null) return;
        String to = story.getAssigned_to().getEmail();
        String subject = "Overdue Task: " + story.getTitle();
        String body = "Your task '" + story.getTitle() + "' is overdue. Please take action.";
        emailService.sendEmail(to, subject, body);
    }

    public void notifySlaBreach(Story story, String slaName) {
        // notify escalationRole if configured, else project manager
        String to = null;
        if (story.getProjectId() != null && story.getProjectId().getManager_id() != null) {
            to = story.getProjectId().getManager_id().getEmail();
        }
        if (to == null && story.getAssigned_to() != null) to = story.getAssigned_to().getEmail();
        if (to == null) return;
        String subject = "SLA Breach: " + story.getTitle();
        String body = "SLA breached for task '" + story.getTitle() + "' (rule: " + slaName + ").";
        emailService.sendEmail(to, subject, body);
    }
}
