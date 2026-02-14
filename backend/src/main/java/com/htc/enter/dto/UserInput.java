package com.htc.enter.dto;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AdminInput.class, name = "admin"),
        @JsonSubTypes.Type(value = EmployeeInput.class, name = "employee"),
        @JsonSubTypes.Type(value = ManagerInput.class, name = "manager"),
        @JsonSubTypes.Type(value = UserInput.class, name = "user")
})
public class UserInput {

    private String type;

    @NotBlank(message = "username is required")
    @Size(max = 100)
    private String username;

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    private String email;

    @NotBlank(message = "password is required")
    @Pattern(regexp = "^[a-zA-Z0-9]{6,12}$", message = "Password must be 6-12 characters long and contain only letters and digits")
    private String password;

    @JsonProperty("job_title")
    private String jobTitle;

    @Past
    @JsonProperty("joining_date")
    private LocalDate joiningDate;

    @JsonProperty("reporting_to_id")
    private Long reportingToId;

    private String department;

    private Integer accessLevel;

    // Getters and setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }

    public Long getReportingToId() { return reportingToId; }
    public void setReportingToId(Long reportingToId) { this.reportingToId = reportingToId; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public Integer getAccessLevel() { return accessLevel; }
    public void setAccessLevel(Integer accessLevel) { this.accessLevel = accessLevel; }
}
