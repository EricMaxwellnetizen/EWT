package com.htc.enter.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class StoryDTO {
    private Long StoryId;
    @NotBlank(message = "title is required")
    private String title;
    private String description;
    private LocalDate dueDate;
    @NotNull(message = "projectId is required")
    private Long projectId;
    private Long assignedToId;
    @NotNull(message = "epicId (workflowStateId) is required")
    private Long workflowStateId;
	private Long createdById;

    // new fields for approval/deadline
    private Boolean isApproved;
    private LocalDate isEnd;
    private LocalDate deadline;
    private String deliverables;
    
    
	public StoryDTO() {
		super();
	}
	public StoryDTO(Long storyId, @NotBlank(message = "title is required") String title, String description,
			LocalDate dueDate, @NotNull(message = "projectId is required") Long projectId, Long assignedToId,
			Long workflowStateId) {
		super();
		StoryId = storyId;
		this.title = title;
		this.description = description;
		this.dueDate = dueDate;
		this.projectId = projectId;
		this.assignedToId = assignedToId;
		this.workflowStateId = workflowStateId;
	}
	public Long getStoryId() {
		return StoryId;
	}
	public void setStoryId(Long storyId) {
		StoryId = storyId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public LocalDate getDueDate() {
		return dueDate;
	}
	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}
	public Long getProjectId() {
		return projectId;
	}
	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}
	public Long getAssignedToId() {
		return assignedToId;
	}
	public void setAssignedToId(Long assignedToId) {
		this.assignedToId = assignedToId;
	}
	public Long getWorkflowStateId() {
		return workflowStateId;
	}
	public void setWorkflowStateId(Long workflowStateId) {
		this.workflowStateId = workflowStateId;
	}
	public Long getCreatedById() {
		return createdById;
	}
	public void setCreatedById(Long createdById) {
		this.createdById = createdById;
	}

    public Boolean getIsApproved() { return isApproved; }
    public void setIsApproved(Boolean isApproved) { this.isApproved = isApproved; }
    public LocalDate getIsEnd() { return isEnd; }
    public void setIsEnd(LocalDate isEnd) { this.isEnd = isEnd; }
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public String getDeliverables() { return deliverables; }
    public void setDeliverables(String deliverables) { this.deliverables = deliverables; }

}