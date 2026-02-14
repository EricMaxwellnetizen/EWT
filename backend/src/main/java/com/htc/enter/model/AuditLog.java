package com.htc.enter.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ewt_audit_logs", indexes = {
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_entity", columnList = "entityType"),
    @Index(name = "idx_audit_operation", columnList = "operation"),
    @Index(name = "idx_audit_user", columnList = "username")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false, length = 100)
    private String entityType;

    @Column(nullable = false)
    private Long entityId;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private OperationType operation;

    @Column(length = 100)
    private String username;

    @Column(length = 50)
    private String ipAddress;

    @Column(columnDefinition = "LONGTEXT")
    private String oldValue;

    @Column(columnDefinition = "LONGTEXT")
    private String newValue;

    @Column(columnDefinition = "LONGTEXT")
    private String changes;

    @Column(length = 500)
    private String description;

    public enum OperationType {
        CREATE, UPDATE, DELETE, READ
    }

    public AuditLog() {
        this.timestamp = Instant.now();
    }

    public AuditLog(String entityType, Long entityId, OperationType operation, String username) {
        this();
        this.entityType = entityType;
        this.entityId = entityId;
        this.operation = operation;
        this.username = username;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public OperationType getOperation() {
        return operation;
    }

    public void setOperation(OperationType operation) {
        this.operation = operation;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getChanges() {
        return changes;
    }

    public void setChanges(String changes) {
        this.changes = changes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
