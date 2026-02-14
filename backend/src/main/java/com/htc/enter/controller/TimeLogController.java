package com.htc.enter.controller;

import com.htc.enter.dto.TimeLogRequest;
import com.htc.enter.model.TimeLog;
import com.htc.enter.service.TimeLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/timelogs")
@RequiredArgsConstructor
public class TimeLogController {
    
    private final TimeLogService timeLogService;
    
    @PostMapping
    public ResponseEntity<TimeLog> logTime(@RequestBody TimeLogRequest request, Authentication authentication) {
        TimeLog timeLog = timeLogService.logTime(request, authentication.getName());
        return ResponseEntity.ok(timeLog);
    }
    
    @GetMapping("/story/{storyId}")
    public ResponseEntity<List<TimeLog>> getTimeLogsForStory(@PathVariable Long storyId) {
        List<TimeLog> timeLogs = timeLogService.getTimeLogsForStory(storyId);
        return ResponseEntity.ok(timeLogs);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TimeLog>> getTimeLogsForUser(@PathVariable Long userId) {
        List<TimeLog> timeLogs = timeLogService.getTimeLogsForUser(userId);
        return ResponseEntity.ok(timeLogs);
    }
    
    @GetMapping("/story/{storyId}/total")
    public ResponseEntity<Double> getTotalHoursForStory(@PathVariable Long storyId) {
        Double total = timeLogService.getTotalHoursForStory(storyId);
        return ResponseEntity.ok(total);
    }
    
    @GetMapping("/user/{userId}/total")
    public ResponseEntity<Double> getTotalHoursForUser(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        Double total = timeLogService.getTotalHoursForUser(userId, startDate, endDate);
        return ResponseEntity.ok(total);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TimeLog> updateTimeLog(@PathVariable Long id, @RequestBody TimeLogRequest request) {
        TimeLog timeLog = timeLogService.updateTimeLog(id, request);
        return ResponseEntity.ok(timeLog);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTimeLog(@PathVariable Long id) {
        timeLogService.deleteTimeLog(id);
        return ResponseEntity.noContent().build();
    }
}
