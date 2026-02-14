package com.htc.enter.dto;

import com.htc.enter.model.Admin.access_Scope;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("admin")
public class AdminOutput extends UserOutput {
    private int adminLevel;
    private access_Scope accessScope;

    public AdminOutput() {}

    public AdminOutput(int adminLevel, access_Scope accessScope) {
        this.adminLevel = adminLevel;
        this.accessScope = accessScope;
    }

    public int getAdminLevel() {
        return adminLevel;
    }

    public void setAdminLevel(int adminLevel) {
        this.adminLevel = adminLevel;
    }

    public access_Scope getAccessScope() {
        return accessScope;
    }

    public void setAccessScope(access_Scope accessScope) {
        this.accessScope = accessScope;
    }
}