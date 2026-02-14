package com.htc.enter.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public class SlaRuleDTO {
    private Long slaId;
    @NotNull(message = "projectId is required")
    private Long projectId;
    @NotNull(message = "stateId is required")
    private Long stateId;
    private int durationHours;
    @NotBlank(message = "startPoint is required")
    private String startPoint;
    private Long escalationRoleId;
    private int escalationDelayHours;
    @NotBlank(message = "priority is required")
    private String priority;
    private boolean notifyEmail;

    public Long getSlaId() { return slaId; }
    public void setSlaId(Long slaId) { this.slaId = slaId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getStateId() { return stateId; }
    public void setStateId(Long stateId) { this.stateId = stateId; }
    public int getDurationHours() { return durationHours; }
    public void setDurationHours(int durationHours) { this.durationHours = durationHours; }
    public String getStartPoint() { return startPoint; }
    public void setStartPoint(String startPoint) { this.startPoint = startPoint; }
    public Long getEscalationRoleId() { return escalationRoleId; }
    public void setEscalationRoleId(Long escalationRoleId) { this.escalationRoleId = escalationRoleId; }
    public int getEscalationDelayHours() { return escalationDelayHours; }
    public void setEscalationDelayHours(int escalationDelayHours) { this.escalationDelayHours = escalationDelayHours; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public boolean isNotifyEmail() { return notifyEmail; }
    public void setNotifyEmail(boolean notifyEmail) { this.notifyEmail = notifyEmail; }
}