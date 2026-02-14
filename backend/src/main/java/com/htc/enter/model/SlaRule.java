package com.htc.enter.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "ewt_sla_rules")
public class SlaRule extends BaseEntity {

    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long slaId;

    @ManyToOne
    @JoinColumn(name = "projectId")
    private Project project;

    @ManyToOne
    @JoinColumn(name = "stateId")
    private Epic state;

    private int durationHours;
    
    private String startPoint; // TASK_CREATION, STATE_ENTRY
    
    @ManyToOne
    @JoinColumn(name = "escalation_role", referencedColumnName = "createdBy")
    private Project escalationRole;
    
    private int escalationDelayHours;
    private String priority; // HIGH, MEDIUM, LOW
    private boolean notifyEmail;

    // Explicit getters/setters to avoid Lombok visibility issues in some toolchains
    public long getSlaId() { return slaId; }
    public void setSlaId(long slaId) { this.slaId = slaId; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public Epic getState() { return state; }
    public void setState(Epic state) { this.state = state; }

    public int getDurationHours() { return durationHours; }
    public void setDurationHours(int durationHours) { this.durationHours = durationHours; }

    public String getStartPoint() { return startPoint; }
    public void setStartPoint(String startPoint) { this.startPoint = startPoint; }

    public Project getEscalationRole() { return escalationRole; }
    public void setEscalationRole(Project escalationRole) { this.escalationRole = escalationRole; }

    public int getEscalationDelayHours() { return escalationDelayHours; }
    public void setEscalationDelayHours(int escalationDelayHours) { this.escalationDelayHours = escalationDelayHours; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public boolean isNotifyEmail() { return notifyEmail; }
    public void setNotifyEmail(boolean notifyEmail) { this.notifyEmail = notifyEmail; }

}