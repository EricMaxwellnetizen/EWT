package com.htc.enter.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.htc.enter.dto.DocumentPayload;
import com.htc.enter.dto.ProjectDTO;
import com.htc.enter.exception.BadRequestException;
import com.htc.enter.exception.ResourceNotFoundException;
import com.htc.enter.mapper.DomainMapper;
import com.htc.enter.model.Project;
import com.htc.enter.model.User;
import com.htc.enter.repository.UserRepository;
import com.htc.enter.service.ProjectAppService;
import com.htc.enter.service.ProjectService;
import com.htc.enter.service.UserAuthService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

/**
 * REST Controller for managing projects in Elara
 * 
 * This controller provides endpoints for:
 * - Creating, reading, updating, and deleting projects
 * - Pagination and sorting of project lists
 * - Downloading project reports for managers
 * 
 * All endpoints require authentication, with role-based access control
 */
@RestController
@RequestMapping("/api/v1/project")
@Validated
public class ProjectController {

    private final ProjectService projectService;
    private final DomainMapper domainMapper;
    private final UserAuthService userAuthService;
    private final ProjectAppService projectAppService;
    private final UserRepository userRepository;

    public ProjectController(
            ProjectService projectService, 
            DomainMapper domainMapper, 
            UserAuthService userAuthService, 
            ProjectAppService projectAppService,
            UserRepository userRepository) {
        this.projectService = projectService;
        this.domainMapper = domainMapper;
        this.userAuthService = userAuthService;
        this.projectAppService = projectAppService;
        this.userRepository = userRepository;
    }

    /**
     * Get all projects (without pagination)
     * 
     * GET /api/v1/project/get
     * 
     * Returns a list of all projects in the system
     * Use the /paginated endpoint for better performance with large datasets
     */
    @GetMapping("/get")
    public ResponseEntity<List<ProjectDTO>> getAllProjects() {
        List<ProjectDTO> allProjects = projectService.findAll().stream()
            .map(domainMapper::toProjectDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(allProjects);
    }

    /**
     * Get projects with pagination and sorting
     * 
     * GET /api/v1/project/paginated?page=0&size=10&sortBy=name&sortDirection=ASC
     * 
     * Query Parameters:
     * - page: Page number (0-based, default: 0)
     * - size: Number of items per page (default: 10)
     * - sortBy: Field to sort by (default: projectId)
     * - sortDirection: Sort direction - ASC or DESC (default: ASC)
     */
    @GetMapping("/paginated")
    public ResponseEntity<Page<ProjectDTO>> getProjectsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "projectId") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        // Determine sort direction
        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC") ? 
                                  Sort.Direction.DESC : 
                                  Sort.Direction.ASC;
        
        // Create pageable request with sorting
        Pageable pageableRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        // Fetch and map projects
        Page<ProjectDTO> projectPage = projectService.findAll(pageableRequest)
            .map(domainMapper::toProjectDTO);
        
        return ResponseEntity.ok(projectPage);
    }

    /**
     * Get a specific project by ID
     * 
     * GET /api/v1/project/{id}
     * 
     * Returns detailed information about a single project
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProjectDTO> getProjectById(@PathVariable Long id) {
        Project foundProject = projectService.findById(id);
        
        if (foundProject == null) {
            throw new ResourceNotFoundException("Project not found with id: " + id);
        }
        
        return ResponseEntity.ok(domainMapper.toProjectDTO(foundProject));
    }

    /**
     * Create a new project
     * 
     * POST /api/v1/project/create
     * 
     * Requires: ROLE_MANAGER or ROLE_ADMIN with access level 3+
     * 
     * Access Rules:
     * - Level 3: Can only create projects for themselves
     * - Level 4: Can create projects for themselves or any user at level 3 and below
     * - Level 5+ (Admin): Can create projects for anyone
     * 
     * Request Body: ProjectDTO with project details
     * - name (required)
     * - managerId (required - restricted based on access level)
     * - clientId, createdById, deliverables, deadline (optional)
     */
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ProjectDTO> createProject(@Valid @RequestBody ProjectDTO projectData) {
        if (projectData == null) {
            throw new BadRequestException("Project data is required");
        }
        
        // Check if user has minimum access level 3
        if (!userAuthService.hasAccessLevel(3)) {
            throw new BadRequestException(
                "Insufficient access level. You need access level 3 or higher to create projects."
            );
        }
        
        var currentUser = userAuthService.getCurrentUser();
        if (currentUser == null) {
            throw new BadRequestException("Authentication required");
        }

        projectData.setCreatedById(currentUser.getId());
        if (currentUser.getAccessLevel() != null && currentUser.getAccessLevel() >= 4) {
            projectData.setIsApproved(true);
        } else {
            projectData.setIsApproved(false);
        }
        
        Integer accessLevel = currentUser.getAccessLevel();
        
        // Level 3: Can only create projects for themselves
        if (accessLevel != null && accessLevel == 3) {
            if (projectData.getManagerId() != null && !projectData.getManagerId().equals(currentUser.getId())) {
                throw new BadRequestException(
                    "Managers with access level 3 can only create projects for themselves."
                );
            }
            if (projectData.getCreatedById() != null && !projectData.getCreatedById().equals(currentUser.getId())) {
                throw new BadRequestException(
                    "Managers with access level 3 can only create projects with themselves as creator."
                );
            }
        }
        
        // Level 4: Can create for themselves or managers at level 3 and below
        else if (accessLevel != null && accessLevel == 4) {
            if (projectData.getManagerId() != null) {
                Long targetManagerId = projectData.getManagerId();
                if (!targetManagerId.equals(currentUser.getId())) {
                    User targetManager = userRepository.findById(targetManagerId)
                            .orElseThrow(() -> new BadRequestException("Target manager not found"));
                    Integer targetLevel = targetManager.getAccessLevel();
                    if (targetLevel == null || targetLevel >= accessLevel) {
                        throw new BadRequestException(
                            "Senior managers can only create projects for themselves or managers at level 3 and below."
                        );
                    }
                }
            }
            if (projectData.getCreatedById() != null && !projectData.getCreatedById().equals(currentUser.getId())) {
                throw new BadRequestException(
                    "Created by ID must be the current user."
                );
            }
        }
        
        // Level 5+: Admin can create for anyone (no restrictions)
        
        // Create the project and send notifications
        Project createdProject = projectAppService.createProject(projectData);
        
        return new ResponseEntity<>(
            domainMapper.toProjectDTO(createdProject), 
            HttpStatus.CREATED
        );
    }

    /**
     * Update an existing project
     * 
     * PUT /api/v1/project/{id}
     * 
     * Requires: ROLE_MANAGER or ROLE_ADMIN, and access level >= 4
     * 
     * Only provided fields in the request body will be updated
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ProjectDTO> updateProject(
            @PathVariable Long id, 
            @Valid @RequestBody ProjectDTO updatedProjectData) {
        
        // Check if user has sufficient access level
        if (!userAuthService.hasAccessLevel(4)) {
            throw new BadRequestException(
                "Insufficient access level. You need access level 4 or higher to update projects."
            );
        }

        Project existingProject = projectService.findById(id);
        if (existingProject == null) {
            throw new ResourceNotFoundException("Project not found with id: " + id);
        }

        User currentUser = userAuthService.getCurrentUser();
        User creator = existingProject.getCreated_by();
        boolean creatorIsSenior = creator != null && creator.getAccessLevel() != null && creator.getAccessLevel() >= 4;
        if (creator == null) {
            creatorIsSenior = true;
        }
        boolean isApproved = existingProject.isIs_approved();

        if (!isApproved && !creatorIsSenior) {
            boolean modifiesWorkFields = updatedProjectData.getDeliverables() != null
                    || updatedProjectData.getIsEnd() != null;
            if (modifiesWorkFields) {
                throw new BadRequestException(
                    "Project cannot be worked on until approved by the creator's reporting manager."
                );
            }
        }

        if (updatedProjectData.getIsApproved() != null && updatedProjectData.getIsApproved()) {
            if (creator == null && currentUser != null) {
                // legacy project without creator; allow senior approval
            } else if (creator != null && creatorIsSenior && currentUser != null
                    && creator.getId().equals(currentUser.getId())) {
                // creator auto-approves
            } else if (creator != null && creator.getReportingTo() != null
                    && currentUser != null
                    && creator.getReportingTo().getId().equals(currentUser.getId())) {
                // reporting manager approves
            } else {
                throw new BadRequestException(
                    "Only the creator's reporting manager can approve this project."
                );
            }
        }
        
        // Update the project
        Project updatedProject = projectAppService.updateProject(id, updatedProjectData);
        
        return ResponseEntity.ok(domainMapper.toProjectDTO(updatedProject));
    }

    /**
     * Delete a project
     * 
     * DELETE /api/v1/project/{id}
     * 
     * Requires: ROLE_ADMIN only
     * 
     * Permanently removes the project from the system
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        // Additional access level check
        if (!userAuthService.hasAccessLevel(4)) {
            throw new BadRequestException(
                "Insufficient access level. You need access level 4 or higher to delete projects."
            );
        }
        
        // Verify project exists before attempting deletion
        Project existingProject = projectService.findById(id);
        if (existingProject == null) {
            throw new ResourceNotFoundException("Project not found with id: " + id);
        }
        
        // Delete the project
        projectAppService.deleteProject(id);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Download a Word document containing all projects for a specific manager
     * 
     * GET /api/v1/project/manager/{managerId}/download
     * 
     * Requires: Authentication and access level 3+ (managers and admins)
     * 
     * Returns: Password-protected Word document with project details
     * Password format: {managerUsername}123
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/manager/{managerId}/download")
    public void downloadManagerProjectsDocument(
            @PathVariable Long managerId, 
            HttpServletResponse response) throws IOException {
        
        // Check if user has permission to access this manager's resources
        // Access level 3+ can access (managers and admins)
        if (!userAuthService.canAccessManagerResources(managerId, 3)) {
            throw new BadRequestException(
                "Access denied. You don't have permission to view this manager's projects."
            );
        }

        // Generate the document with all projects for this manager
        DocumentPayload documentPayload = projectAppService.buildManagerProjectsDocument(managerId);

        // Set response headers for file download
        response.setContentType(documentPayload.getContentType());
        response.setHeader("Content-Disposition", 
            "attachment; filename=\"" + 
            URLEncoder.encode(documentPayload.getFilename(), StandardCharsets.UTF_8.toString()) + 
            "\"");
        
        // Write the document to the response
        response.getOutputStream().write(documentPayload.getContent());
        response.getOutputStream().flush();
    }
}