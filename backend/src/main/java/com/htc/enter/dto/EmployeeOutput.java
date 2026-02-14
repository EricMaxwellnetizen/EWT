package com.htc.enter.dto;

import com.htc.enter.model.Employee.skill_set;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("employee")
public class EmployeeOutput extends UserOutput {
    private int maxActiveTasks;
    private skill_set skillSet;

    public EmployeeOutput() {}

    public EmployeeOutput(int maxActiveTasks, skill_set skillSet) {
        this.maxActiveTasks = maxActiveTasks;
        this.skillSet = skillSet;
    }

    public int getMaxActiveTasks() {
        return maxActiveTasks;
    }

    public void setMaxActiveTasks(int maxActiveTasks) {
        this.maxActiveTasks = maxActiveTasks;
    }

    public skill_set getSkillSet() {
        return skillSet;
    }

    public void setSkillSet(skill_set skillSet) {
        this.skillSet = skillSet;
    }
}