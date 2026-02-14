package com.htc.enter.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Data;

@Entity
@Table(name= "ewt_epic")
@Data
public class Epic extends BaseEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long epicId;
	
	@ManyToOne
	@JoinColumn(name = "ProjectId")
	private Project projectId;
	
	@ManyToOne
	@JoinColumn(name = "managerId", nullable = false)
	private User manager_id;

	@ManyToOne
	@JoinColumn(name = "createdBy")
	private User created_by;
	
	private String name;
	private LocalDate is_start;
	private LocalDate is_end; // set automatically when approved
	
	// new fields
	private LocalDate deadline;
	private boolean is_approved;
	private String deliverables;

	// Automatic behavior: when approved, set is_end if not set
	@PrePersist
	@PreUpdate
	private void handleApproval() {
		if (this.is_approved && this.is_end == null) {
			this.is_end = LocalDate.now();
		}
	}

	// Explicit getters/setters to avoid Lombok/IDE mismatch
	public long getEpicId() { return epicId; }
	public void setEpicId(long epicId) { this.epicId = epicId; }
	public Project getProjectId() { return projectId; }
	public void setProjectId(Project projectId) { this.projectId = projectId; }
	public User getManager_id() { return manager_id; }
	public void setManager_id(User manager_id) { this.manager_id = manager_id; }
	public User getCreated_by() { return created_by; }
	public void setCreated_by(User created_by) { this.created_by = created_by; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public LocalDate getIs_start() { return is_start; }
	public void setIs_start(LocalDate is_start) { this.is_start = is_start; }
	public LocalDate getIs_end() { return is_end; }
	public void setIs_end(LocalDate is_end) { this.is_end = is_end; }
	public LocalDate getDeadline() { return deadline; }
	public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
	public boolean isIs_approved() { return is_approved; }
	public void setIs_approved(boolean is_approved) { this.is_approved = is_approved; }
	public String getDeliverables() { return deliverables; }
	public void setDeliverables(String deliverables) { this.deliverables = deliverables; }

	// Convenience getters for compatibility
	public Project getProject() { return this.getProjectId(); }
	public User getManager() { return this.getManager_id(); }
	public Boolean getIsApproved() { return this.isIs_approved(); }
	
}