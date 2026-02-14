package com.htc.enter.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EpicDTO {
    @JsonProperty("workflowStateId")
    private Long EpicId;
    @NotNull(message = "projectId is required")
    @JsonProperty("projectId")
    private Long projectId;
    @NotNull(message = "managerId is required")
    @JsonProperty("managerId")
    private Long managerId;
    @JsonProperty("createdById")
    private Long createdById;
    @NotBlank(message = "name is required")
    @JsonProperty("name")
    private String name;
    @JsonProperty("isStart")
    private LocalDate isStart;
    @JsonProperty("isEnd")
    private LocalDate isEnd;
    @JsonProperty("deadline")
    private LocalDate deadline;
    @JsonProperty("isApproved")
    private Boolean isApproved;
    @JsonProperty("deliverables")
    private String deliverables;

    public Long getWorkflowStateId() { return EpicId; }
    public void setWorkflowStateId(Long workflowStateId) { this.EpicId = workflowStateId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getManagerId() { return managerId; }
    public void setManagerId(Long managerId) { this.managerId = managerId; }
    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getIsStart() { return isStart; }
    public void setIsStart(LocalDate isStart) { this.isStart = isStart; }
    public LocalDate getIsEnd() { return isEnd; }
    public void setIsEnd(LocalDate isEnd) { this.isEnd = isEnd; }
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public Boolean getIsApproved() { return isApproved; }
    public void setIsApproved(Boolean isApproved) { this.isApproved = isApproved; }
    public String getDeliverables() { return deliverables; }
    public void setDeliverables(String deliverables) { this.deliverables = deliverables; }
}