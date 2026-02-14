package com.htc.enter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for report generation requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {
    
    private String reportType; // PROJECT, USER, CLIENT, TASK
    private Long entityId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String format; // PDF, EXCEL, JSON
}
