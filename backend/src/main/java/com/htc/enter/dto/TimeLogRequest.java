package com.htc.enter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeLogRequest {
    private Long storyId;
    private Double hoursWorked;
    private String description;
    private LocalDateTime workDate;
}
