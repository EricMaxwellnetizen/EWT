package com.htc.enter.serviceimpl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;

import com.htc.enter.dto.ProjectDTO;
import com.htc.enter.model.Project;
import com.htc.enter.model.Client;
import com.htc.enter.model.User;
import com.htc.enter.repository.ClientRepository;
import com.htc.enter.repository.ProjectRepository;
import com.htc.enter.repository.UserRepository;
import com.htc.enter.service.ProjectService;
import com.htc.enter.service.ProjectNotificationService;

/**
 * Service implementation for managing projects in Elara
 * 
 * This service handles all business logic related to projects including:
 * - Creating and updating projects with proper validation
 * - Transactional integrity with rollback support
 * - Caching frequently accessed projects for better performance
 * - Sending email notifications to project managers
 * - Managing relationships between projects, clients, and users
 */
@Service
public class ProjectServiceImpl implements ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);

    private final ProjectRepository projectRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final ProjectNotificationService notificationService;

    public ProjectServiceImpl(
            ProjectRepository projectRepository, 
            ClientRepository clientRepository, 
            UserRepository userRepository, 
            ProjectNotificationService notificationService) {
        this.projectRepository = projectRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    /**
     * Save or update a project
     * Clears the cache to ensure fresh data on next read
     * Uses transaction with rollback on any exception
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"projects", "projectsByManager"}, allEntries = true)
    public Project save(Project project) {
        log.info("Saving project: {}", project.getName());
        return projectRepository.save(project);
    }

    /**
     * Find a project by its ID
     * Results are cached to improve performance for frequently accessed projects
     */
    @Override
    @Cacheable(value = "projects", key = "#projectId")
    public Project findById(Long projectId) {
        return projectRepository.findById(projectId).orElse(null);
    }

    /**
     * Get all projects in the system
     * Results are cached for better performance
     */
    @Override
    @Cacheable(value = "projects")
    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    /**
     * Get all projects with pagination support
     * Useful for displaying large lists of projects in the UI
     */
    @Override
    public Page<Project> findAll(Pageable pageable) {
        return projectRepository.findAll(pageable);
    }

    /**
     * Delete a project by its ID
     * Clears all project-related caches to maintain data consistency
     * Uses transaction to ensure atomic deletion with rollback support
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
        @CacheEvict(value = "projects", allEntries = true),
        @CacheEvict(value = "projectsByManager", allEntries = true)
    })
    public void deleteById(Long projectId) {
        log.info("Deleting project with ID: {}", projectId);
        projectRepository.deleteById(projectId);
    }

    /**
     * Create a new project from a DTO (Data Transfer Object)
     * 
     * This method:
     * 1. Validates and resolves relationships (client, creator, manager)
     * 2. Saves the project to the database (transactional with rollback)
     * 3. Sends an email notification to the project manager
     * 4. Clears the cache to ensure fresh data
     * 
     * Transaction will rollback if any exception occurs during the process
     */
    @Override
    @Transactional(
        rollbackFor = Exception.class,
        isolation = Isolation.READ_COMMITTED,
        propagation = Propagation.REQUIRED
    )
    @CacheEvict(value = {"projects", "projectsByManager"}, allEntries = true)
    public Project createFromDTO(ProjectDTO projectData) {
        log.info("Creating new project: {}", projectData.getName());
        // Create a new project instance
        Project newProject = new Project();
        
        // Set basic project information
        newProject.setName(projectData.getName());
        
        if (projectData.getDeliverables() != null) {
            newProject.setDeliverables(projectData.getDeliverables());
        }
        
        if (projectData.getDeadline() != null) {
            newProject.setDeadline(projectData.getDeadline());
        }
        
        // Set approval status (default to false if not provided)
        newProject.setIs_approved(
            projectData.getIsApproved() != null ? projectData.getIsApproved() : false
        );
        
        if (projectData.getIsEnd() != null) {
            newProject.setIs_end(projectData.getIsEnd());
        }

        // Resolve and set the client relationship
        if (projectData.getClientId() != null) {
            Client associatedClient = clientRepository.findById(projectData.getClientId())
                    .orElseThrow(() -> new IllegalArgumentException(
                        "Client not found with id: " + projectData.getClientId()
                    ));
            newProject.setClient_id(associatedClient);
        }

        // Resolve and set the project creator
        if (projectData.getCreatedById() != null) {
            User projectCreator = userRepository.findById(projectData.getCreatedById())
                    .orElseThrow(() -> new IllegalArgumentException(
                        "Creator user not found with id: " + projectData.getCreatedById()
                    ));
            newProject.setCreated_by(projectCreator);
        }

        // Resolve and set the project manager (required field)
        if (projectData.getManagerId() != null) {
            User projectManager = userRepository.findById(projectData.getManagerId())
                    .orElseThrow(() -> new IllegalArgumentException(
                        "Manager user not found with id: " + projectData.getManagerId()
                    ));
            newProject.setManager_id(projectManager);
        } else {
            throw new IllegalArgumentException("Manager ID is required for creating a project");
        }

        // Save the project to the database
        Project savedProject = projectRepository.save(newProject);
        log.info("Project created successfully with ID: {}", savedProject.getProjectId());
        
        // Send email notification to the project manager
        // This happens asynchronously so it won't block the response
        if (savedProject.isIs_approved()) {
            notificationService.notifyManagerOnProjectCreation(savedProject);
        }
        
        return savedProject;
    }


    /**
     * Update an existing project from a DTO
     * 
     * Only updates fields that are provided in the DTO (non-null values)
     * Clears the cache to ensure fresh data on next read
     * Transaction ensures atomicity with rollback on failure
     */
    @Override
    @Transactional(
        rollbackFor = Exception.class,
        isolation = Isolation.READ_COMMITTED,
        propagation = Propagation.REQUIRED
    )
    @CacheEvict(value = {"projects", "projectsByManager"}, allEntries = true)
    public Project updateFromDTO(Long projectId, ProjectDTO updatedData) {
        log.info("Updating project with ID: {}", projectId);
        // Find the existing project
        Project existingProject = findById(projectId);
        if (existingProject == null) {
            throw new IllegalArgumentException("Project not found with id: " + projectId);
        }
        
        // Update basic fields if provided
        if (updatedData.getName() != null) {
            existingProject.setName(updatedData.getName());
        }
        
        // Update client relationship if provided
        if (updatedData.getClientId() != null) {
            Client updatedClient = clientRepository.findById(updatedData.getClientId()).orElse(null);
            existingProject.setClient_id(updatedClient);
        }
        
        // Update creator if provided
        if (updatedData.getCreatedById() != null) {
            User updatedCreator = userRepository.findById(updatedData.getCreatedById()).orElse(null);
            existingProject.setCreated_by(updatedCreator);
        }
        
        // Update manager if provided
        if (updatedData.getManagerId() != null) {
            User updatedManager = userRepository.findById(updatedData.getManagerId()).orElse(null);
            existingProject.setManager_id(updatedManager);
        }
        
        // Update optional fields with safe handling
        try { 
            if (updatedData.getDeliverables() != null) {
                existingProject.setDeliverables(updatedData.getDeliverables());
            }
        } catch (Exception e) { 
            // Log but continue - deliverables update is optional
        }
        
        try { 
            if (updatedData.getDeadline() != null) {
                existingProject.setDeadline(updatedData.getDeadline());
            }
        } catch (Exception e) { 
            // Log but continue - deadline update is optional
        }
        
        try { 
            existingProject.setIs_approved(
                updatedData.getIsApproved() != null ? 
                updatedData.getIsApproved() : 
                existingProject.isIs_approved()
            );
        } catch (Exception e) { 
            // Log but continue - approval flag update is optional
        }
        
        try { 
            if (updatedData.getIsEnd() != null) {
                existingProject.setIs_end(updatedData.getIsEnd());
            }
        } catch (Exception e) { 
            // Log but continue - end date update is optional
        }

        Project updatedProject = projectRepository.save(existingProject);
        log.info("Project updated successfully: {}", updatedProject.getName());
        
        return updatedProject;
    }

    /**
     * Find all projects managed by a specific manager
     * Results are cached to improve performance for frequently accessed manager data
     */
    @Override
    @Cacheable(value = "projectsByManager", key = "#managerId")
    public List<Project> findByManagerId(Long managerId) {
        return projectRepository.findByManagerId(managerId);
    }

    /**
     * Find all projects managed by a specific manager with pagination
     * Useful for displaying large lists of projects in the UI
     */
    @Override
    public Page<Project> findByManagerId(Long managerId, Pageable pageable) {
        return projectRepository.findByManagerId(managerId, pageable);
    }
}
