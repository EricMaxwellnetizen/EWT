package com.htc.enter.service;

import com.htc.enter.dto.TimeLogRequest;
import com.htc.enter.model.Story;
import com.htc.enter.model.TimeLog;
import com.htc.enter.model.User;
import com.htc.enter.repository.StoryRepository;
import com.htc.enter.repository.TimeLogRepository;
import com.htc.enter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimeLogService {
    
    private final TimeLogRepository timeLogRepository;
    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public TimeLog logTime(TimeLogRequest request, String username) {
        Story story = storyRepository.findById(request.getStoryId())
            .orElseThrow(() -> new RuntimeException("Story not found"));
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        TimeLog timeLog = new TimeLog();
        timeLog.setStory(story);
        timeLog.setUser(user);
        timeLog.setHoursWorked(request.getHoursWorked());
        timeLog.setDescription(request.getDescription());
        timeLog.setWorkDate(request.getWorkDate() != null ? request.getWorkDate() : LocalDateTime.now());
        
        return timeLogRepository.save(timeLog);
    }
    
    public List<TimeLog> getTimeLogsForStory(Long storyId) {
        return timeLogRepository.findByStoryIdOrderByWorkDateDesc(storyId);
    }
    
    public List<TimeLog> getTimeLogsForUser(Long userId) {
        return timeLogRepository.findByUserIdOrderByWorkDateDesc(userId);
    }
    
    public Double getTotalHoursForStory(Long storyId) {
        Double total = timeLogRepository.getTotalHoursForStory(storyId);
        return total != null ? total : 0.0;
    }
    
    public Double getTotalHoursForUser(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        Double total = timeLogRepository.getTotalHoursForUser(userId, startDate, endDate);
        return total != null ? total : 0.0;
    }
    
    @Transactional
    public TimeLog updateTimeLog(Long id, TimeLogRequest request) {
        TimeLog timeLog = timeLogRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("TimeLog not found"));
        
        timeLog.setHoursWorked(request.getHoursWorked());
        timeLog.setDescription(request.getDescription());
        if (request.getWorkDate() != null) {
            timeLog.setWorkDate(request.getWorkDate());
        }
        
        return timeLogRepository.save(timeLog);
    }
    
    @Transactional
    public void deleteTimeLog(Long id) {
        timeLogRepository.deleteById(id);
    }
}
