package com.htc.enter.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonTypeName;

@Entity
@Table(name = "ewt_admin")
@PrimaryKeyJoinColumn(name = "id")
@JsonTypeName("admin")
public class Admin extends User {

    private int admin_level;

    @Enumerated(EnumType.STRING)
    private access_Scope accessScope;

    public enum access_Scope {
        ALL,
        DEPARTMENT,
        PROJECT
    }

    public int getAdmin_level() {
        return admin_level;
    }

    public void setAdmin_level(int admin_level) {
        this.admin_level = admin_level;
    }

    public access_Scope getAccessScope() {
        return accessScope;
    }

    public void setAccessScope(access_Scope accessScope) {
        this.accessScope = accessScope;
    }

}