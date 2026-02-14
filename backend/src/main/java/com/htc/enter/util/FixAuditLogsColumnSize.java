package com.htc.enter.util;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Database maintenance utility for audit log storage
 * Upgrades column types to support larger data payloads
 * Can be removed after successful execution
 */
@Component
public class FixAuditLogsColumnSize implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public FixAuditLogsColumnSize(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Checking audit logs column configuration...");
        
        try {
            String checkColumnType = 
                "SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_NAME = 'ewt_audit_logs' " +
                "AND COLUMN_NAME = 'new_value' " +
                "AND TABLE_SCHEMA = DATABASE()";
            
            String currentType = jdbcTemplate.queryForObject(checkColumnType, String.class);
            
            if ("longtext".equalsIgnoreCase(currentType)) {
                System.out.println("Audit log columns are already configured correctly");
                return;
            }
            
            System.out.println("Upgrading column types from " + currentType + " to LONGTEXT for better capacity");
            
            jdbcTemplate.execute("ALTER TABLE ewt_audit_logs MODIFY COLUMN old_value LONGTEXT");
            jdbcTemplate.execute("ALTER TABLE ewt_audit_logs MODIFY COLUMN new_value LONGTEXT");
            jdbcTemplate.execute("ALTER TABLE ewt_audit_logs MODIFY COLUMN changes LONGTEXT");
            
            System.out.println("Successfully upgraded audit log column types");
            System.out.println("Note: This file can be safely removed after first successful run");
            
        } catch (Exception e) {
            System.err.println("Failed to upgrade audit log columns: " + e.getMessage());
            System.err.println("Manual SQL execution may be required:");
            System.err.println("  ALTER TABLE ewt_audit_logs MODIFY COLUMN old_value LONGTEXT;");
            System.err.println("  ALTER TABLE ewt_audit_logs MODIFY COLUMN new_value LONGTEXT;");
            System.err.println("  ALTER TABLE ewt_audit_logs MODIFY COLUMN changes LONGTEXT;");
        }
    }
}
