package com.htc.enter.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonTypeName;

@Entity
@Table(name = "ewt_employee")
@PrimaryKeyJoinColumn(name = "id")
@JsonTypeName("employee")
public class Employee extends User {

    private int max_active_tasks;

    @Enumerated(EnumType.STRING)
    private skill_set skillSet;

    public enum skill_set{JAVA, SPRINGBOOT, REACT, ANGULAR, PYTHON, NUMPY, PANDAS, MATPLOTLIB, 
        SEABORNE, TENSORFLOW, KERAS, SQL, BIGQUERY, AWS, GCP}

    public int getMax_active_tasks() {
        return max_active_tasks;
    }

    public void setMax_active_tasks(int max_active_tasks) {
        this.max_active_tasks = max_active_tasks;
    }

    public skill_set getSkillSet() {
        return skillSet;
    }

    public void setSkillSet(skill_set skillSet) {
        this.skillSet = skillSet;
    }
}