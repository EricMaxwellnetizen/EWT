package com.htc.enter.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.htc.enter.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    Page<Notification> findByUserId(Long userId, Pageable pageable);
    
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.isRead = false ORDER BY n.createdAt DESC LIMIT 5")
    List<Notification> findRecent5Unread(@Param("userId") Long userId);
    
    long countByUserIdAndIsReadFalse(Long userId);
    
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.createdAt >= :since ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdAndCreatedAfter(@Param("userId") Long userId, @Param("since") LocalDateTime since);
    
    void deleteByUserIdAndCreatedAtBefore(Long userId, LocalDateTime cutoffDate);
}
