package com.htc.enter.dto;

public class ManagerInput extends UserInput {

    private Double approvalLimit;
    private Integer managedWorkflowCount;
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
