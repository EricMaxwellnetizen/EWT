package com.htc.enter.repository;

import com.htc.enter.model.TimeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TimeLogRepository extends JpaRepository<TimeLog, Long> {
    
    @Query("SELECT tl FROM TimeLog tl WHERE tl.story.storyId = :storyId ORDER BY tl.workDate DESC")
    List<TimeLog> findByStoryIdOrderByWorkDateDesc(@Param("storyId") Long storyId);
    
    @Query("SELECT tl FROM TimeLog tl WHERE tl.user.id = :userId ORDER BY tl.workDate DESC")
    List<TimeLog> findByUserIdOrderByWorkDateDesc(@Param("userId") Long userId);
    
    @Query("SELECT tl FROM TimeLog tl WHERE tl.story.storyId = :storyId AND tl.workDate BETWEEN :startDate AND :endDate")
    List<TimeLog> findByStoryAndDateRange(@Param("storyId") Long storyId, 
                                          @Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(tl.hoursWorked) FROM TimeLog tl WHERE tl.story.storyId = :storyId")
    Double getTotalHoursForStory(@Param("storyId") Long storyId);
    
    @Query("SELECT SUM(tl.hoursWorked) FROM TimeLog tl WHERE tl.user.id = :userId AND tl.workDate BETWEEN :startDate AND :endDate")
    Double getTotalHoursForUser(@Param("userId") Long userId, 
                                 @Param("startDate") LocalDateTime startDate, 
                                 @Param("endDate") LocalDateTime endDate);
}
