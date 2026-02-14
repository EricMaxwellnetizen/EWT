package com.htc.enter.service;

import com.htc.enter.model.*;
import com.htc.enter.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class AuditLogMigrationService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private final AtomicBoolean migrationCompleted = new AtomicBoolean(false);

    /**
     * Automatically runs on application startup to backfill historical audit data
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void migrateHistoricalData() {
        System.out.println("================================================================================");
        System.out.println("AUDIT LOG MIGRATION - Startup Check");
        System.out.println("================================================================================");
        
        if (migrationCompleted.get()) {
            System.out.println("Historical audit log migration already completed in this session");
            System.out.println("================================================================================");
            return;
        }

        long existingLogs = auditLogRepository.count();
        System.out.println("Current audit log count: " + existingLogs);
        
        if (existingLogs > 0) {
            System.out.println("Audit logs already contain " + existingLogs + " entries. Skipping migration.");
            System.out.println("================================================================================");
            migrationCompleted.set(true);
            return;
        }

        System.out.println("Starting historical audit log migration...");
        System.out.println("================================================================================");

        try {
            int totalMigrated = 0;

            // Migrate all entity types
            totalMigrated += migrateEntity("User", "ewt_user");
            totalMigrated += migrateEntity("Admin", "ewt_admin");
            totalMigrated += migrateEntity("Employee", "ewt_employee");
            totalMigrated += migrateEntity("Manager", "ewt_manager");
            totalMigrated += migrateEntity("Project", "ewt_projects");
            totalMigrated += migrateEntity("Story", "ewt_story");
            totalMigrated += migrateEntity("Epic", "ewt_epic");
            totalMigrated += migrateEntity("Client", "ewt_client");
            totalMigrated += migrateEntity("SlaRule", "ewt_sla_rules");
            totalMigrated += migrateEntity("Notification", "notifications");
            totalMigrated += migrateEntity("TokenRecord", "ewt_tokens");

            migrationCompleted.set(true);
            System.out.println("Historical audit log migration completed!");
            System.out.println("Total records migrated: " + totalMigrated);

        } catch (Exception e) {
            System.err.println("Error during audit log migration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Migrates records from a specific database table to audit logs
     */
    @SuppressWarnings("unchecked")
    private int migrateEntity(String entityType, String tableName) {
        try {
            System.out.println("  Migrating " + entityType + " from table " + tableName + "...");

            String query = "SELECT * FROM " + tableName;
            List<Object[]> results = entityManager.createNativeQuery(query).getResultList();

            if (results.isEmpty()) {
                System.out.println("    No records found in " + tableName);
                return 0;
            }

            // Get column names
            List<String> columnNames = getColumnNames(tableName);

            int migratedCount = 0;
            for (Object[] row : results) {
                try {
                    // Extract key fields from the row
                    Long id = extractId(row, columnNames);
                    Instant createdAt = extractInstant(row, columnNames, "created_at");
                    Instant updatedAt = extractInstant(row, columnNames, "updated_at");
                    String createdBy = extractString(row, columnNames, "created_by");

                    if (id == null) continue;

                    // Create audit log for creation
                    if (createdAt != null) {
                        AuditLog createLog = new AuditLog();
                        createLog.setEntityType(entityType);
                        createLog.setEntityId(id);
                        createLog.setOperation(AuditLog.OperationType.CREATE);
                        createLog.setTimestamp(createdAt);
                        createLog.setUsername(createdBy != null ? createdBy : "system");
                        createLog.setDescription("Historical: Created " + entityType);
                        createLog.setIpAddress("migration");
                        
                        // Try to serialize the entity data
                        try {
                            String entityData = createEntityJsonFromRow(row, columnNames);
                            createLog.setNewValue(entityData);
                        } catch (Exception e) {
                            // Ignore serialization errors
                        }

                        auditLogRepository.save(createLog);
                        migratedCount++;
                    }

                    // Create audit log for last update (if different from creation)
                    if (updatedAt != null && createdAt != null && !updatedAt.equals(createdAt)) {
                        AuditLog updateLog = new AuditLog();
                        updateLog.setEntityType(entityType);
                        updateLog.setEntityId(id);
                        updateLog.setOperation(AuditLog.OperationType.UPDATE);
                        updateLog.setTimestamp(updatedAt);
                        updateLog.setUsername(createdBy != null ? createdBy : "system");
                        updateLog.setDescription("Historical: Updated " + entityType);
                        updateLog.setIpAddress("migration");

                        auditLogRepository.save(updateLog);
                        migratedCount++;
                    }

                } catch (Exception e) {
                    System.err.println("    Error migrating row in " + tableName + ": " + e.getMessage());
                }
            }

            System.out.println("    Migrated " + migratedCount + " audit entries for " + entityType);
            return migratedCount;

        } catch (Exception e) {
            System.err.println("    Error migrating " + entityType + ": " + e.getMessage());
            return 0;
        }
    }

    /**
     * Retrieves column names for the specified table
     */
    @SuppressWarnings("unchecked")
    private List<String> getColumnNames(String tableName) {
        String query = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                      "WHERE TABLE_NAME = '" + tableName + "' " +
                      "AND TABLE_SCHEMA = DATABASE() " +
                      "ORDER BY ORDINAL_POSITION";
        
        return entityManager.createNativeQuery(query).getResultList();
    }

    /**
     * Extract ID from row
     */
    private Long extractId(Object[] row, List<String> columnNames) {
        int idIndex = columnNames.indexOf("id");
        if (idIndex >= 0 && idIndex < row.length && row[idIndex] != null) {
            Object idValue = row[idIndex];
            if (idValue instanceof Number) {
                return ((Number) idValue).longValue();
            }
        }
        return null;
    }

    /**
     * Extract Instant timestamp from row
     */
    private Instant extractInstant(Object[] row, List<String> columnNames, String columnName) {
        int index = columnNames.indexOf(columnName);
        if (index >= 0 && index < row.length && row[index] != null) {
            Object value = row[index];
            if (value instanceof java.sql.Timestamp) {
                return ((java.sql.Timestamp) value).toInstant();
            } else if (value instanceof java.time.LocalDateTime) {
                return ((java.time.LocalDateTime) value).atZone(java.time.ZoneId.systemDefault()).toInstant();
            } else if (value instanceof Instant) {
                return (Instant) value;
            }
        }
        return null;
    }

    /**
     * Extract String from row
     */
    private String extractString(Object[] row, List<String> columnNames, String columnName) {
        int index = columnNames.indexOf(columnName);
        if (index >= 0 && index < row.length && row[index] != null) {
            return row[index].toString();
        }
        return null;
    }

    /**
     * Create a simple JSON representation from row data
     */
    private String createEntityJsonFromRow(Object[] row, List<String> columnNames) {
        StringBuilder json = new StringBuilder("{");
        for (int i = 0; i < Math.min(row.length, columnNames.size()); i++) {
            if (i > 0) json.append(",");
            String columnName = columnNames.get(i);
            Object value = row[i];
            
            json.append("\"").append(columnName).append("\":");
            
            if (value == null) {
                json.append("null");
            } else if (value instanceof String) {
                json.append("\"").append(value.toString().replace("\"", "\\\"")).append("\"");
            } else if (value instanceof Number || value instanceof Boolean) {
                json.append(value);
            } else {
                json.append("\"").append(value.toString().replace("\"", "\\\"")).append("\"");
            }
        }
        json.append("}");
        return json.toString();
    }
}
