package com.htc.enter.dto;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = AdminOutput.class, name = "admin"),
    @JsonSubTypes.Type(value = EmployeeOutput.class, name = "employee"),
    @JsonSubTypes.Type(value = ManagerOutput.class, name = "manager")
})
public class UserOutput {

    private Long id;
    private String username;
    private String email;

    @JsonProperty("job_title")
    private String jobTitle;

    @JsonProperty("joining_date")
    private LocalDate joiningDate;

    @JsonProperty("reporting_to_id")
    private Long reportingToId;

    private String reportingToUsername;
    private String department;
    private Integer accessLevel;
    private String role;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }

    public Long getReportingToId() { return reportingToId; }
    public void setReportingToId(Long reportingToId) { this.reportingToId = reportingToId; }

    public String getReportingToUsername() { return reportingToUsername; }
    public void setReportingToUsername(String reportingToUsername) { this.reportingToUsername = reportingToUsername; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public Integer getAccessLevel() { return accessLevel; }
    public void setAccessLevel(Integer accessLevel) { this.accessLevel = accessLevel; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
