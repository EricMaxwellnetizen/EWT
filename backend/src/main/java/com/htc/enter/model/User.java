package com.htc.enter.model;

import java.time.LocalDate;
import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@Entity
@Table(name = "ewt_user")
@Inheritance(strategy = InheritanceType.JOINED)
// JSON polymorphic type info so entities include a `type` field (admin|employee|manager|user)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Admin.class, name = "admin"),
        @JsonSubTypes.Type(value = Employee.class, name = "employee"),
        @JsonSubTypes.Type(value = Manager.class, name = "manager"),
        @JsonSubTypes.Type(value = User.class, name = "user")
})
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = false)
    private String email;

    @Column(nullable = false)
    private String passwordhash;

    @Column(name = "job_title")
    private String job_title;

    @Column(name = "joining_date")
    private LocalDate joining_date;

    @ManyToOne
    @JoinColumn(name = "reporting_to", nullable = true) // ensure nullable
    private User reportingTo;


    @Column(name = "department")
    private String department;

    @Column(name = "role", nullable = true)
    private String role;

    // Profile picture URL
    @Column(name = "profile_picture_url", length = 500)
    private String profilePictureUrl;

    // OAuth2 fields
    @Column(name = "oauth2_enabled")
    private Boolean oauth2Enabled = false;

    @Column(name = "oauth2_provider", length = 50)
    private String oauth2Provider;

    // new access level for authorization (1..n)
    @Column(name = "access_level")
    private Integer access_level;

    public User() {
        assignRole();
        if (this.access_level == null) {
            if (this instanceof Admin) this.access_level = 5;
            else if (this instanceof Manager) this.access_level = 4;
            else if (this instanceof Employee) this.access_level = 2;
            else this.access_level = 1;
        }
        // Ensure oauth2Enabled is never null
        if (this.oauth2Enabled == null) {
            this.oauth2Enabled = false;
        }
    }

    @PrePersist
    @PreUpdate
    private void assignRole() {
        if (this instanceof Admin) {
            this.role = "admin";
        } else if (this instanceof Employee) {
            this.role = "employee";
        } else if (this instanceof Manager) {
            this.role = "manager";
        } else {
            this.role = "user";
        }

        // Normalize role to uppercase to avoid case-sensitivity issues when mapping to authorities
        if (this.role != null) {
            this.role = this.role.trim().toUpperCase();
        }
        
        // Ensure oauth2Enabled is never null before persist/update
        if (this.oauth2Enabled == null) {
            this.oauth2Enabled = false;
        }
    }

    public String getUsername() {
        return username;
    }

    public Long getId() {
		return id;
	}

        // Compatibility getter used across codebase
        public Long getUserId() { return this.getId(); }

	public void setId(Long id) {
		this.id = id;
	}

	public void setUsername(String username) {
        this.username = username;
        assignRole();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        assignRole();
    }

    public String getPasswordhash() {
        return passwordhash;
    }

    public void setPasswordhash(String passwordhash) {
        this.passwordhash = passwordhash;
        assignRole();
    }

    public String getJob_title() {
        return job_title;
    }

    public void setJob_title(String job_title) {
        this.job_title = job_title;
    }

    public LocalDate getJoining_date() {
        return joining_date;
    }

    public void setJoining_date(LocalDate joining_date) {
        this.joining_date = joining_date;
    }

    public User getReportingTo() {
        return reportingTo;
    }

    public void setReportingTo(User reportingTo) {
        this.reportingTo = reportingTo;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getRole() {
        if (this.role == null) return null;
        return this.role.trim().toUpperCase();
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    // access level accessor using camelCase to follow Java conventions
    public Integer getAccessLevel() {
        return access_level;
    }

    public void setAccessLevel(Integer accessLevel) {
        this.access_level = accessLevel;
    }

    public boolean isOAuth2Enabled() {
        return Boolean.TRUE.equals(oauth2Enabled);
    }

    public void setOAuth2Enabled(Boolean oauth2Enabled) {
        this.oauth2Enabled = oauth2Enabled;
    }

    public String getOAuth2Provider() {
        return oauth2Provider;
    }

    public void setOAuth2Provider(String oauth2Provider) {
        this.oauth2Provider = oauth2Provider;
    }

	
}