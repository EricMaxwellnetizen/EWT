package com.htc.enter.notification;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import com.htc.enter.model.Story;
import com.htc.enter.model.SlaRule;
import com.htc.enter.repository.StoryRepository;
import com.htc.enter.repository.SlaRuleRepository;

@Component
public class ScheduledNotifier {

    private final StoryRepository storyRepo;
    private final SlaRuleRepository slaRepo;
    private final NotificationService notificationService;
    private final Logger log = LoggerFactory.getLogger(ScheduledNotifier.class);

    public ScheduledNotifier(StoryRepository storyRepo, SlaRuleRepository slaRepo, NotificationService notificationService) {
        this.storyRepo = storyRepo;
        this.slaRepo = slaRepo;
        this.notificationService = notificationService;
    }

    // run every 5 minutes
    @Scheduled(fixedDelay = 300000)
    public void checkOverdueAndSla() {
        try {
            List<Story> stories = storyRepo.findAll();
            LocalDate now = LocalDate.now();
            for (Story s : stories) {
                if (s.getDueDate() != null && s.getDueDate().isBefore(now) && !s.isIs_approved()) {
                    notificationService.notifyOverdue(s);
                }
            }

            // simple SLA check: for each SLA rule, find stories in that state and check createdAt + duration
            List<SlaRule> slas = slaRepo.findAll();
            for (SlaRule rule : slas) {
                if (!rule.isNotifyEmail()) continue;
                if (rule.getState() == null) continue;
                List<Story> storiesInState = storyRepo.findAll(); // simple for now - could add query by state
                for (Story s : storiesInState) {
                    if (s.getEpicId() == null) continue;
                    if (s.getEpicId().getEpicId() != rule.getState().getEpicId()) continue;
                    // if durationHours elapsed since creation
                    if (s.getCreatedAt() != null) {
                        long hours = ChronoUnit.HOURS.between(s.getCreatedAt(), java.time.LocalDateTime.now());
                        if (hours > rule.getDurationHours()) {
                            notificationService.notifySlaBreach(s, "SLA#" + rule.getSlaId());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("ScheduledNotifier failed: {}", e.getMessage());
        }
    }
}
