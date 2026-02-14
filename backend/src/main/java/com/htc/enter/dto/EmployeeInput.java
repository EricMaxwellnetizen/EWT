package com.htc.enter.dto;

import java.util.Set;

public class EmployeeInput extends UserInput {

    private Integer maxActiveTasks;
    private Set<String> skillSet;

    public Integer getMaxActiveTasks() {
        return maxActiveTasks;
    }

    public void setMaxActiveTasks(Integer maxActiveTasks) {
        this.maxActiveTasks = maxActiveTasks;
    }

    public Set<String> getSkillSet() {
        return skillSet;
    }

    public void setSkillSet(Set<String> skillSet) {
        this.skillSet = skillSet;
    }
}
