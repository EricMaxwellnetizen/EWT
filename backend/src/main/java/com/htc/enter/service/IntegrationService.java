package com.htc.enter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntegrationService {
    
    private final WebClient.Builder webClientBuilder;
    
    @Value("${integration.slack.webhook.url:}")
    private String slackWebhookUrl;
    
    @Value("${integration.teams.webhook.url:}")
    private String teamsWebhookUrl;
    
    @Value("${integration.enabled:false}")
    private Boolean integrationsEnabled;
    
    /**
     * Send notification to Slack
     */
    public void sendToSlack(String message, String channel) {
        if (!integrationsEnabled || slackWebhookUrl == null || slackWebhookUrl.isEmpty()) {
            log.debug("Slack integration disabled or not configured");
            return;
        }
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("text", message);
        if (channel != null && !channel.isEmpty()) {
            payload.put("channel", channel);
        }
        
        try {
            webClientBuilder.build()
                .post()
                .uri(slackWebhookUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    log.error("Error sending Slack notification", e);
                    return Mono.empty();
                })
                .subscribe(response -> log.debug("Slack notification sent successfully"));
        } catch (Exception e) {
            log.error("Error sending Slack notification", e);
        }
    }
    
    /**
     * Send notification to Microsoft Teams
     */
    public void sendToTeams(String title, String message) {
        if (!integrationsEnabled || teamsWebhookUrl == null || teamsWebhookUrl.isEmpty()) {
            log.debug("Teams integration disabled or not configured");
            return;
        }
        
        // Teams Adaptive Card format
        Map<String, Object> payload = new HashMap<>();
        payload.put("@type", "MessageCard");
        payload.put("@context", "https://schema.org/extensions");
        payload.put("summary", title);
        payload.put("themeColor", "0078D7");
        payload.put("title", title);
        payload.put("text", message);
        
        try {
            webClientBuilder.build()
                .post()
                .uri(teamsWebhookUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    log.error("Error sending Teams notification", e);
                    return Mono.empty();
                })
                .subscribe(response -> log.debug("Teams notification sent successfully"));
        } catch (Exception e) {
            log.error("Error sending Teams notification", e);
        }
    }
    
    /**
     * Send notification to all configured integrations
     */
    public void sendToAll(String title, String message) {
        sendToSlack(title + ": " + message, null);
        sendToTeams(title, message);
    }
    
    /**
     * Send story created notification
     */
    public void notifyStoryCreated(String projectName, String storyTitle, String assignedTo) {
        String message = String.format("New story created in project '%s': %s (Assigned to: %s)", 
            projectName, storyTitle, assignedTo);
        sendToAll("Story Created", message);
    }
    
    /**
     * Send story completed notification
     */
    public void notifyStoryCompleted(String projectName, String storyTitle, String completedBy) {
        String message = String.format("Story completed in project '%s': %s (Completed by: %s)", 
            projectName, storyTitle, completedBy);
        sendToAll("Story Completed", message);
    }
    
    /**
     * Send SLA breach notification
     */
    public void notifySLABreach(String projectName, String slaName) {
        String message = String.format("⚠️ SLA breach detected! Project: %s, SLA: %s", 
            projectName, slaName);
        sendToAll("SLA Breach Alert", message);
    }
}
