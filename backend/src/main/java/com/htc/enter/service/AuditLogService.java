package com.htc.enter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htc.enter.model.AuditLog;
import com.htc.enter.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Log an audit entry asynchronously
     * Captures entity changes with before/after snapshots and field-by-field diffs
     * 
     * @param entityType - Type of entity (e.g., "User", "Project")
     * @param entityId - ID of the affected entity
     * @param operation - Type of operation (CREATE, UPDATE, DELETE, READ)
     * @param oldValue - Entity state before operation (null for CREATE)
     * @param newValue - Entity state after operation (null for DELETE)
     * @param description - Human-readable description of the operation
     */
    @Async
    public void logOperation(String entityType, Long entityId, AuditLog.OperationType operation, 
                            Object oldValue, Object newValue, String description) {
        try {
            AuditLog auditLogEntry = new AuditLog();
            auditLogEntry.setEntityType(entityType);
            auditLogEntry.setEntityId(entityId);
            auditLogEntry.setOperation(operation);
            auditLogEntry.setTimestamp(Instant.now());
            
            // Get current username from security context
            String authenticatedUsername = getCurrentUsername();
            auditLogEntry.setUsername(authenticatedUsername);
            
            // Get IP address from request
            String clientIpAddress = getCurrentIpAddress();
            auditLogEntry.setIpAddress(clientIpAddress);
            
            // Serialize old and new values to JSON
            if (oldValue != null) {
                String oldValueJson = objectMapper.writeValueAsString(oldValue);
                auditLogEntry.setOldValue(oldValueJson);
            }
            if (newValue != null) {
                String newValueJson = objectMapper.writeValueAsString(newValue);
                auditLogEntry.setNewValue(newValueJson);
            }
            
            // Calculate and store field-by-field changes
            if (oldValue != null && newValue != null) {
                String changesJson = calculateChanges(oldValue, newValue);
                auditLogEntry.setChanges(changesJson);
            }
            
            auditLogEntry.setDescription(description);
            
            auditLogRepository.save(auditLogEntry);
        } catch (JsonProcessingException jsonException) {
            // Log error but don't fail the main operation
            System.err.println("Failed to serialize audit log values: " + jsonException.getMessage());
        } catch (Exception generalException) {
            System.err.println("Failed to save audit log: " + generalException.getMessage());
        }
    }

    /**
     * Get all audit logs with pagination and sorting
     * 
     * @param pageable - Pagination and sorting parameters
     * @return Paginated audit log entries
     */
    public Page<AuditLog> getAllLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    /**
     * Search audit logs with multiple optional filters
     * All filter parameters are optional - null values are ignored
     * 
     * @param entityType - Filter by entity type (e.g., "User", "Project")
     * @param operation - Filter by operation type (CREATE, UPDATE, DELETE, READ)
     * @param username - Filter by username (partial match supported)
     * @param startDate - Filter by date range start (inclusive)
     * @param endDate - Filter by date range end (inclusive)
     * @param pageable - Pagination and sorting parameters
     * @return Paginated filtered audit log entries
     */
    public Page<AuditLog> searchLogs(String entityType, AuditLog.OperationType operation, 
                                    String username, Instant startDate, Instant endDate, 
                                    Pageable pageable) {
        return auditLogRepository.searchLogs(entityType, operation, username, startDate, endDate, pageable);
    }

    /**
     * Get complete audit history for a specific entity
     * Returns all log entries for one entity, ordered by timestamp descending
     * 
     * @param entityType - Type of entity (e.g., "User", "Project")
     * @param entityId - ID of the entity
     * @param pageable - Pagination parameters
     * @return Paginated audit history for the entity
     */
    public Page<AuditLog> getLogsForEntity(String entityType, Long entityId, Pageable pageable) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId, pageable);
    }

    /**
     * Get most recent audit log entries
     * Useful for displaying recent activity dashboards
     * 
     * @return Last 100 audit log entries, ordered by timestamp descending
     */
    public List<AuditLog> getRecentLogs() {
        return auditLogRepository.findTop100ByOrderByTimestampDesc();
    }

    /**
     * Get aggregated statistics about audit logs
     * Returns counts grouped by operation type and entity type
     * 
     * @return Map containing statistics:
     *         - byOperation: Map of operation type -> count
     *         - byEntityType: Map of entity type -> count
     *         - totalLogs: Total number of audit log entries
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> statisticsMap = new HashMap<>();
        
        List<Object[]> operationCountRows = auditLogRepository.countByOperation();
        Map<String, Long> operationStatistics = new HashMap<>();
        for (Object[] rowData : operationCountRows) {
            operationStatistics.put(rowData[0].toString(), (Long) rowData[1]);
        }
        statisticsMap.put("byOperation", operationStatistics);
        
        List<Object[]> entityCountRows = auditLogRepository.countByEntityType();
        Map<String, Long> entityTypeStatistics = new HashMap<>();
        for (Object[] rowData : entityCountRows) {
            entityTypeStatistics.put((String) rowData[0], (Long) rowData[1]);
        }
        statisticsMap.put("byEntityType", entityTypeStatistics);
        
        statisticsMap.put("totalLogs", auditLogRepository.count());
        
        return statisticsMap;
    }

    /**
     * Get current username from Spring Security context
     * Returns "system" if no authenticated user is found
     * 
     * @return Username of the authenticated user or "system"
     */
    private String getCurrentUsername() {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
        } catch (Exception authException) {
            // Silently ignore - return default
        }
        return "system";
    }

    /**
     * Get client IP address from HTTP request
     * Checks X-Forwarded-For header first (for proxy/load balancer scenarios)
     * Falls back to direct remote address
     * Returns "unknown" if request context is not available
     * 
     * @return Client IP address or "unknown"
     */
    private String getCurrentIpAddress() {
        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                HttpServletRequest httpRequest = requestAttributes.getRequest();
                String forwardedIpAddress = httpRequest.getHeader("X-Forwarded-For");
                if (forwardedIpAddress == null || forwardedIpAddress.isEmpty()) {
                    forwardedIpAddress = httpRequest.getRemoteAddr();
                }
                return forwardedIpAddress;
            }
        } catch (Exception requestException) {
            // Silently ignore - return default
        }
        return "unknown";
    }

    /**
     * Calculate field-by-field differences between old and new entity values
     * Compares JSON representations and creates a human-readable diff
     * 
     * Format for changes:
     * - "Added: <value>" for new fields
     * - "Removed: <value>" for deleted fields
     * - "<oldValue> → <newValue>" for modified fields
     * 
     * @param oldValue - Entity state before change
     * @param newValue - Entity state after change
     * @return JSON string containing field-by-field changes
     * @throws JsonProcessingException if serialization fails
     */
    private String calculateChanges(Object oldValue, Object newValue) throws JsonProcessingException {
        @SuppressWarnings("unchecked")
        Map<String, Object> oldValueMap = objectMapper.convertValue(oldValue, Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> newValueMap = objectMapper.convertValue(newValue, Map.class);
        
        Map<String, String> fieldChanges = new HashMap<>();
        for (String fieldName : newValueMap.keySet()) {
            Object oldFieldValue = oldValueMap.get(fieldName);
            Object newFieldValue = newValueMap.get(fieldName);
            
            if (oldFieldValue == null && newFieldValue != null) {
                fieldChanges.put(fieldName, "Added: " + newFieldValue);
            } else if (oldFieldValue != null && newFieldValue == null) {
                fieldChanges.put(fieldName, "Removed: " + oldFieldValue);
            } else if (oldFieldValue != null && !oldFieldValue.equals(newFieldValue)) {
                fieldChanges.put(fieldName, oldFieldValue + " → " + newFieldValue);
            }
        }
        
        return objectMapper.writeValueAsString(fieldChanges);
    }
}
