package com.htc.enter.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for client with nested hierarchy: Client -> Projects -> Epics -> Stories -> Users
 */
public class ClientHierarchyDTO {
    
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private String address;
    private LocalDateTime createdAt;
    private List<ProjectHierarchyDTO> projects;
    
    // Nested DTO for Project within hierarchy
    public static class ProjectHierarchyDTO {
        private Long id;
        private String name;
        private String description;
        private LocalDateTime deadline;
        private Boolean isApproved;
        private UserBasicDTO manager;
        private Integer progress;
        private List<EpicHierarchyDTO> epics;
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public LocalDateTime getDeadline() { return deadline; }
        public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
        
        public Boolean getIsApproved() { return isApproved; }
        public void setIsApproved(Boolean isApproved) { this.isApproved = isApproved; }
        
        public UserBasicDTO getManager() { return manager; }
        public void setManager(UserBasicDTO manager) { this.manager = manager; }
        
        public Integer getProgress() { return progress; }
        public void setProgress(Integer progress) { this.progress = progress; }
        
        public List<EpicHierarchyDTO> getEpics() { return epics; }
        public void setEpics(List<EpicHierarchyDTO> epics) { this.epics = epics; }
    }
    
    // Nested DTO for Epic within hierarchy
    public static class EpicHierarchyDTO {
        private Long id;
        private String name;
        private String description;
        private LocalDateTime dueDate;
        private Boolean isApproved;
        private UserBasicDTO manager;
        private Integer progress;
        private List<StoryHierarchyDTO> stories;
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public LocalDateTime getDueDate() { return dueDate; }
        public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
        
        public Boolean getIsApproved() { return isApproved; }
        public void setIsApproved(Boolean isApproved) { this.isApproved = isApproved; }
        
        public UserBasicDTO getManager() { return manager; }
        public void setManager(UserBasicDTO manager) { this.manager = manager; }
        
        public Integer getProgress() { return progress; }
        public void setProgress(Integer progress) { this.progress = progress; }
        
        public List<StoryHierarchyDTO> getStories() { return stories; }
        public void setStories(List<StoryHierarchyDTO> stories) { this.stories = stories; }
    }
    
    // Nested DTO for Story within hierarchy
    public static class StoryHierarchyDTO {
        private Long id;
        private String name;
        private String description;
        private String status;
        private LocalDateTime dueDate;
        private Boolean isApproved;
        private UserBasicDTO assignedTo;
        private Double estimatedHours;
        private Double actualHours;
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public LocalDateTime getDueDate() { return dueDate; }
        public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
        
        public Boolean getIsApproved() { return isApproved; }
        public void setIsApproved(Boolean isApproved) { this.isApproved = isApproved; }
        
        public UserBasicDTO getAssignedTo() { return assignedTo; }
        public void setAssignedTo(UserBasicDTO assignedTo) { this.assignedTo = assignedTo; }
        
        public Double getEstimatedHours() { return estimatedHours; }
        public void setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; }
        
        public Double getActualHours() { return actualHours; }
        public void setActualHours(Double actualHours) { this.actualHours = actualHours; }
    }
    
    // Nested DTO for basic user info
    public static class UserBasicDTO {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String role;
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
    
    // Main DTO getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public List<ProjectHierarchyDTO> getProjects() { return projects; }
    public void setProjects(List<ProjectHierarchyDTO> projects) { this.projects = projects; }
}
