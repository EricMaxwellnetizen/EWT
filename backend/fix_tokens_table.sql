-- Fix ewt_tokens table to add AUTO_INCREMENT to id column
-- Run this script on your MySQL database to fix the login issue

-- Check current table structure
DESC ewt_tokens;

-- Modify the id column to be AUTO_INCREMENT
ALTER TABLE ewt_tokens MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;

-- Verify the change
DESC ewt_tokens;

-- Optional: Clear any existing tokens if needed
-- TRUNCATE TABLE ewt_tokens;
