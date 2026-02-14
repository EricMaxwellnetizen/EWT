package com.htc.enter.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "ewt_story")
public class Story extends BaseEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long storyId;
	
	@ManyToOne
	@JoinColumn(name = "projectId")
	private Project projectId;
	
	@ManyToOne
	@JoinColumn(name = "EpicId")
	private Epic EpicId;
	
	@ManyToOne
	@JoinColumn(name = "assignedTo")
	private User assigned_to;

	@ManyToOne
	@JoinColumn(name = "createdBy")
	private User created_by;
	
	private String title;
	
	private String deliverables; // renamed from description
	
	private LocalDate dueDate;

	// new fields
	private LocalDate deadline;
	private boolean is_approved;
	private LocalDate is_end;
    
	// optional time tracking fields
	private Double estimatedHours;
	private Double actualHours;

	public long getStoryId() {
		return storyId;
	}

	public void setStoryId(long storyId) {
		this.storyId = storyId;
	}

	public Project getProjectId() {
		return projectId;
	}

	public void setProjectId(Project projectId) {
		this.projectId = projectId;
	}

	public Epic getEpicId() {
		return EpicId;
	}

	public void setEpicId(Epic epicId) {
		EpicId = epicId;
	}

	// Manager is derived from project's manager_id, not stored separately
	public User getManager() {
		return (projectId != null) ? projectId.getManager_id() : null;
	}

	public User getAssigned_to() {
		return assigned_to;
	}

	public void setAssigned_to(User assigned_to) {
		this.assigned_to = assigned_to;
	}

	public User getCreated_by() {
		return created_by;
	}

	public void setCreated_by(User created_by) {
		this.created_by = created_by;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDeliverables() {
		return deliverables;
	}

	public void setDeliverables(String deliverables) {
		this.deliverables = deliverables;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}

	public LocalDate getDeadline() { return deadline; }
	public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
	public boolean isIs_approved() { return is_approved; }
	public void setIs_approved(boolean is_approved) { this.is_approved = is_approved; }
	public LocalDate getIs_end() { return is_end; }
	public void setIs_end(LocalDate is_end) { this.is_end = is_end; }
	public Double getEstimatedHours() { return estimatedHours; }
	public void setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; }
	public Double getActualHours() { return actualHours; }
	public void setActualHours(Double actualHours) { this.actualHours = actualHours; }

	public Story(long storyId, Project projectId, Epic epicId, User assigned_to, String title,
			String deliverables, LocalDate dueDate) {
		super();
		this.storyId = storyId;
		this.projectId = projectId;
		EpicId = epicId;
		this.assigned_to = assigned_to;
		this.title = title;
		this.deliverables = deliverables;
		this.dueDate = dueDate;
	}

	public Story() {
		super();
	}

	// Compatibility convenience getters (camelCase) used across older code
	public Project getProject() { return this.getProjectId(); }
	public Epic getEpic() { return this.getEpicId(); }
	public User getAssignee() { return this.getAssigned_to(); }
	public String getDescription() { return this.getDeliverables(); }
	public Boolean getIsApproved() { return this.isIs_approved(); }
	
	
	

}