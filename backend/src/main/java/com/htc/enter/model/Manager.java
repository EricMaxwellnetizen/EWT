package com.htc.enter.model;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonTypeName;

@Entity
@Table(name = "ewt_manager")
@PrimaryKeyJoinColumn(name = "id")
@JsonTypeName("manager")
public class Manager extends User {

    @Column(name = "approval_limit")
    private Double approvalLimit;

    @Column(name = "managed_workflow_count")
    private Integer managedWorkflowCount;
    
    @Column(name = "team_size")
    private Integer teamSize;

    public Double getApprovalLimit() {
        return approvalLimit;
    }

    public void setApprovalLimit(Double approvalLimit) {
        this.approvalLimit = approvalLimit;
    }

    public Integer getManagedWorkflowCount() {
        return managedWorkflowCount;
    }

    public void setManagedWorkflowCount(Integer managedWorkflowCount) {
        this.managedWorkflowCount = managedWorkflowCount;
    }

    public Integer getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(Integer teamSize) {
        this.teamSize = teamSize;
    }
}