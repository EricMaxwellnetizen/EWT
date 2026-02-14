package com.htc.enter.controller;

import com.htc.enter.model.AuditLog;
import com.htc.enter.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    /**
     * Get all audit logs with pagination
     */
    @GetMapping
    public ResponseEntity<Page<AuditLog>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? 
            Sort.by(sortBy).ascending() : 
            Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<AuditLog> logs = auditLogService.getAllLogs(pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * Search audit logs with filters
     */
    @GetMapping("/search")
    public ResponseEntity<Page<AuditLog>> searchLogs(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) AuditLog.OperationType operation,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logs = auditLogService.searchLogs(
            entityType, operation, username, startDate, endDate, pageable
        );
        return ResponseEntity.ok(logs);
    }

    /**
     * Get logs for a specific entity
     */
    @GetMapping("/{entityType}/{entityId}")
    public ResponseEntity<Page<AuditLog>> getLogsForEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logs = auditLogService.getLogsForEntity(entityType, entityId, pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get recent logs (last 100)
     */
    @GetMapping("/recent")
    public ResponseEntity<List<AuditLog>> getRecentLogs() {
        List<AuditLog> logs = auditLogService.getRecentLogs();
        return ResponseEntity.ok(logs);
    }

    /**
     * Get audit log statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = auditLogService.getStatistics();
        return ResponseEntity.ok(stats);
    }
}
