package com.htc.enter.model;

import jakarta.annotation.Generated;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "ewt_projects")
public class Project extends BaseEntity{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long projectId;
	
	private String name;
	
	@ManyToOne
	@JoinColumn(name = "clientId")
	private Client client_id;
	
	@ManyToOne
	@JoinColumn(name = "createdBy")
	private User created_by;
	
	@ManyToOne
	@JoinColumn(name = "managerId", nullable = false)
	private User manager_id;

	private String deliverables;

	// new fields
	private LocalDate deadline;
	private boolean is_approved;
	private LocalDate is_end; 
	
	@PrePersist
	@PreUpdate
	private void syncManagerReporting() {
		if (this.manager_id != null && this.created_by != null
				&& this.manager_id.getId() != this.created_by.getId()) {
			this.manager_id.setReportingTo(this.created_by);
		}
		if (this.is_approved && this.is_end == null) {
			this.is_end = LocalDate.now();
		}
	}
	
	public long getProjectId() { return projectId; }
	public void setProjectId(long projectId) { this.projectId = projectId; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public Client getClient_id() { return client_id; }
	public void setClient_id(Client client_id) { this.client_id = client_id; }
	public User getCreated_by() { return created_by; }
	public void setCreated_by(User created_by) { this.created_by = created_by; }
	public User getManager_id() { return manager_id; }
	public void setManager_id(User manager_id) { this.manager_id = manager_id; }
	public String getDeliverables() { return deliverables; }
	public void setDeliverables(String deliverables) { this.deliverables = deliverables; }
	public LocalDate getDeadline() { return deadline; }
	public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
	public boolean isIs_approved() { return is_approved; }
	public void setIs_approved(boolean is_approved) { this.is_approved = is_approved; }
	public LocalDate getIs_end() { return is_end; }
	public void setIs_end(LocalDate is_end) { this.is_end = is_end; }

	// Convenience / compatibility getters (camelCase) for older code expecting different names
	public Client getClient() { return this.getClient_id(); }
	public User getProjectManager() { return this.getManager_id(); }
	public Boolean getIsApproved() { return this.isIs_approved(); }
	public Long getProjectIdLong() { return Long.valueOf(this.projectId); }
	
}