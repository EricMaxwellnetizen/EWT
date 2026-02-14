package com.htc.enter.repository;

import com.htc.enter.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    // Find all logs for a specific entity type
    Page<AuditLog> findByEntityTypeOrderByTimestampDesc(String entityType, Pageable pageable);
    
    // Find all logs for a specific entity
    Page<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, Long entityId, Pageable pageable);
    
    // Find logs by operation type
    Page<AuditLog> findByOperationOrderByTimestampDesc(AuditLog.OperationType operation, Pageable pageable);
    
    // Find logs by username
    Page<AuditLog> findByUsernameOrderByTimestampDesc(String username, Pageable pageable);
    
    // Find logs within a time range
    Page<AuditLog> findByTimestampBetweenOrderByTimestampDesc(Instant start, Instant end, Pageable pageable);
    
    // Search logs with filters
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:entityType IS NULL OR a.entityType = :entityType) AND " +
           "(:operation IS NULL OR a.operation = :operation) AND " +
           "(:username IS NULL OR a.username LIKE %:username%) AND " +
           "(:startDate IS NULL OR a.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR a.timestamp <= :endDate) " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> searchLogs(
        @Param("entityType") String entityType,
        @Param("operation") AuditLog.OperationType operation,
        @Param("username") String username,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        Pageable pageable
    );
    
    // Get recent logs (last N logs)
    List<AuditLog> findTop100ByOrderByTimestampDesc();
    
    // Count logs by operation type for statistics
    @Query("SELECT a.operation, COUNT(a) FROM AuditLog a GROUP BY a.operation")
    List<Object[]> countByOperation();
    
    // Count logs by entity type for statistics
    @Query("SELECT a.entityType, COUNT(a) FROM AuditLog a GROUP BY a.entityType")
    List<Object[]> countByEntityType();
}
