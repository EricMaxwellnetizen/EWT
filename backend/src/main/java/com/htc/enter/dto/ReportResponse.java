package com.htc.enter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for report responses from reporting service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    
    private boolean success;
    private String message;
    private String reportId;
    private String downloadUrl;
    private Object data; // Generic data field for analytics
}
