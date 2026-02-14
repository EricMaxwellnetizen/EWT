package com.htc.enter.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Database maintenance utility for token table structure
 * Ensures the tokens table has proper auto-increment configuration
 */
@Component
public class FixTokensTableRunner implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            System.out.println("Checking tokens table structure...");
            
            String alterTableSQL = "ALTER TABLE ewt_tokens MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT";
            
            jdbcTemplate.execute(alterTableSQL);
            
            System.out.println("Successfully updated tokens table structure");
            System.out.println("Note: This file can be safely removed after first successful run");
            
        } catch (Exception e) {
            System.err.println("Error updating tokens table: " + e.getMessage());
            System.err.println("The table might already be configured correctly");
        }
    }
}
