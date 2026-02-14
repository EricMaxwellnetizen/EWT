package com.htc.enter.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Database maintenance utility for epic table structure.
 * Ensures the ewt_epic table has proper auto-increment on epic_id.
 */
@Component
public class FixEpicTableRunner implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            System.out.println("Checking ewt_epic table structure...");

            // Check if epic_id already has AUTO_INCREMENT
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SELECT EXTRA FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ewt_epic' AND COLUMN_NAME = 'epic_id'"
            );

            if (!columns.isEmpty()) {
                String extra = String.valueOf(columns.get(0).get("EXTRA"));
                if (extra != null && extra.toLowerCase().contains("auto_increment")) {
                    System.out.println("ewt_epic.epic_id already has AUTO_INCREMENT - no changes needed");
                    return;
                }
            }

            // Find all foreign keys referencing ewt_epic.epic_id
            List<Map<String, Object>> fks = jdbcTemplate.queryForList(
                "SELECT CONSTRAINT_NAME, TABLE_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
                "WHERE REFERENCED_TABLE_SCHEMA = DATABASE() " +
                "AND REFERENCED_TABLE_NAME = 'ewt_epic' AND REFERENCED_COLUMN_NAME = 'epic_id'"
            );

            // Drop each foreign key constraint
            for (Map<String, Object> fk : fks) {
                String constraintName = String.valueOf(fk.get("CONSTRAINT_NAME"));
                String tableName = String.valueOf(fk.get("TABLE_NAME"));
                System.out.println("Dropping FK " + constraintName + " on " + tableName);
                jdbcTemplate.execute("ALTER TABLE " + tableName + " DROP FOREIGN KEY " + constraintName);
            }

            // Now modify the column
            jdbcTemplate.execute("ALTER TABLE ewt_epic MODIFY COLUMN epic_id BIGINT NOT NULL AUTO_INCREMENT");
            System.out.println("Successfully added AUTO_INCREMENT to ewt_epic.epic_id");

            // Re-add the foreign keys
            for (Map<String, Object> fk : fks) {
                String constraintName = String.valueOf(fk.get("CONSTRAINT_NAME"));
                String tableName = String.valueOf(fk.get("TABLE_NAME"));
                System.out.println("Re-adding FK " + constraintName + " on " + tableName);
                jdbcTemplate.execute(
                    "ALTER TABLE " + tableName + " ADD CONSTRAINT " + constraintName +
                    " FOREIGN KEY (epic_id) REFERENCES ewt_epic(epic_id)"
                );
            }

            System.out.println("Epic table fix complete");
        } catch (Exception e) {
            System.err.println("Error updating ewt_epic table: " + e.getMessage());
            System.err.println("The table might already be configured correctly");
        }
    }
}
