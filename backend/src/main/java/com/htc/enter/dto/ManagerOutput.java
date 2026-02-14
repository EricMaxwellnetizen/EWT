package com.htc.enter.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("manager")
public class ManagerOutput extends UserOutput {
    private Double approvalLimit;
    private int managedWorkflowCount;
    private int teamSize;

    public ManagerOutput() {}

    public ManagerOutput(Double approvalLimit, int managedWorkflowCount, int teamSize) {
        this.approvalLimit = approvalLimit;
        this.managedWorkflowCount = managedWorkflowCount;
        this.teamSize = teamSize;
    }

    public Double getApprovalLimit() {
        return approvalLimit;
    }

    public void setApprovalLimit(Double approvalLimit) {
        this.approvalLimit = approvalLimit;
    }

    public int getManagedWorkflowCount() {
        return managedWorkflowCount;
    }

    public void setManagedWorkflowCount(int managedWorkflowCount) {
        this.managedWorkflowCount = managedWorkflowCount;
    }

    public int getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(int teamSize) {
        this.teamSize = teamSize;
    }
}