package com.htc.enter.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import com.htc.enter.dto.DocumentPayload;
import com.htc.enter.dto.EpicDTO;
import com.htc.enter.exception.BadRequestException;
import com.htc.enter.exception.ResourceNotFoundException;
import com.htc.enter.mapper.DomainMapper;
import com.htc.enter.model.Epic;
import com.htc.enter.model.Project;
import com.htc.enter.model.User;
import com.htc.enter.repository.ProjectRepository;
import com.htc.enter.repository.UserRepository;
import com.htc.enter.service.EpicAppService;
import com.htc.enter.service.EpicService;
import com.htc.enter.service.UserAuthService;

@RestController
@RequestMapping("/api/v1/epic")
@Validated
public class EpicsController {

    private final EpicService service;
    private final DomainMapper mapper;
    private final ProjectRepository projectRepo;
    private final UserRepository userRepo;
    private final UserAuthService authService;
    private final EpicAppService epicAppService;

    public EpicsController(EpicService service, DomainMapper mapper, UserRepository userRepo, 
                          ProjectRepository projectRepo, UserAuthService authService,
                          EpicAppService epicAppService) {
        this.service = service;
        this.mapper = mapper;
        this.userRepo = userRepo;
        this.projectRepo = projectRepo;
        this.authService = authService;
        this.epicAppService = epicAppService;
    }

    @GetMapping("/get")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EpicDTO>> getAll() {
        // Access level 2+ can view epics
        if (!authService.hasAccessLevel(2)) {
            throw new BadRequestException("Insufficient access level to view epics.");
        }
        List<EpicDTO> list = service.findAll().stream().map(mapper::toWorkflowStateDTO).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EpicDTO> getById(@PathVariable Long id) {
        // Access level 2+ can view epics
        if (!authService.hasAccessLevel(2)) {
            throw new BadRequestException("Insufficient access level to view epics.");
        }
        Epic ws = service.findById(id);
        if (ws == null) throw new ResourceNotFoundException("Epic not found with id: " + id);
        return ResponseEntity.ok(mapper.toWorkflowStateDTO(ws));
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<EpicDTO> create(@Valid @RequestBody EpicDTO dto) {
        if (dto == null) throw new BadRequestException("Epic payload required");
        
        // Require access level 3+ (managers and above - employees cannot create epics)
        if (!authService.hasAccessLevel(3)) {
            throw new BadRequestException(
                "Insufficient access level. You need access level 3 or higher to create epics."
            );
        }
        
        var currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new BadRequestException("Authentication required");
        }

        dto.setCreatedById(currentUser.getId());
        if (currentUser.getAccessLevel() != null && currentUser.getAccessLevel() >= 4) {
            dto.setIsApproved(true);
        } else {
            dto.setIsApproved(false);
        }
        
        Integer accessLevel = currentUser.getAccessLevel();
        
        // Level 3: Can only create epics for themselves
        if (accessLevel != null && accessLevel == 3) {
            if (dto.getManagerId() != null && !dto.getManagerId().equals(currentUser.getId())) {
                throw new BadRequestException(
                    "Managers with access level 3 can only create epics for themselves."
                );
            }
        }
        
        // Level 4: Can create for themselves or their direct reports
        else if (accessLevel != null && accessLevel == 4) {
            if (dto.getManagerId() != null) {
                Long targetManagerId = dto.getManagerId();
                if (!targetManagerId.equals(currentUser.getId()) && 
                    !authService.isDirectReport(targetManagerId, currentUser.getId())) {
                    throw new BadRequestException(
                        "Senior managers can only create epics for themselves or their direct reports."
                    );
                }
            }
        }
        
        // Level 5+: Can create for anyone
        
        Project p = null;
        if (dto.getProjectId() != null) p = projectRepo.findById(dto.getProjectId()).orElse(null);
        checkProjectPermission(p);
        Epic saved = service.createFromDTO(dto);
        return new ResponseEntity<>(mapper.toWorkflowStateDTO(saved), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<EpicDTO> update(@PathVariable Long id, @Valid @RequestBody EpicDTO dto) {
        Epic existing = service.findById(id);
        if (existing == null) throw new ResourceNotFoundException("Epic not found with id: " + id);
        
        // Require access level 2+ (employees and above)
        if (!authService.hasAccessLevel(2)) {
            throw new BadRequestException(
                "Insufficient access level. You need access level 2 or higher to update epics."
            );
        }
        
        User current = authService.getCurrentUser();
        Project p = existing.getProjectId();
        checkProjectPermission(p);
        User creator = existing.getCreated_by();
        if (creator == null && p != null) {
            creator = p.getCreated_by();
        }
        boolean creatorIsSenior = creator != null && creator.getAccessLevel() != null && creator.getAccessLevel() >= 4;
        boolean isApproved = existing.isIs_approved();

        if (!isApproved && !creatorIsSenior) {
            boolean modifiesWorkFields = dto.getDeliverables() != null || dto.getIsStart() != null || dto.getIsEnd() != null;
            if (modifiesWorkFields) {
                throw new BadRequestException(
                    "Epic cannot be worked on until approved by the creator's reporting manager."
                );
            }
        }

        if (dto.getIsApproved() != null && dto.getIsApproved()) {
            if (creator != null && creatorIsSenior && current != null && creator.getId().equals(current.getId())) {
                // creator auto-approves
            } else if (creator != null && creator.getReportingTo() != null
                    && current != null
                    && creator.getReportingTo().getId().equals(current.getId())) {
                // reporting manager approves
            } else {
                throw new BadRequestException("Only the creator's reporting manager can approve epics");
            }
        }
        Epic saved = service.updateFromDTO(id, dto);
        return ResponseEntity.ok(mapper.toWorkflowStateDTO(saved));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Epic existing = service.findById(id);
        if (existing == null) throw new ResourceNotFoundException("Epic not found with id: " + id);
        
        // Require access level 2+ (employees and above)
        if (!authService.hasAccessLevel(2)) {
            throw new BadRequestException(
                "Insufficient access level. You need access level 2 or higher to delete epics."
            );
        }
        
        User current = authService.getCurrentUser();
        Project p = existing.getProjectId();
        checkProjectPermission(p);
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Download a Word document containing all epics for a specific manager
     * 
     * GET /api/v1/epic/manager/{managerId}/download
     * 
     * Requires: Authentication and access level 2+ (employees and above)
     * 
     * Returns: Password-protected Word document with epic details
     * Password format: {username}123
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/manager/{managerId}/download")
    public void downloadManagerEpicsDocument(
            @PathVariable Long managerId, 
            HttpServletResponse response) throws IOException {
        
        // Check if user has minimum access level 2
        if (!authService.hasAccessLevel(2)) {
            throw new BadRequestException(
                "Access denied. You need access level 2 or higher to download epic reports."
            );
        }
        
        // Check if user has permission to access this manager's resources
        if (!authService.canAccessManagerResources(managerId, 2)) {
            throw new BadRequestException(
                "Access denied. You don't have permission to view this manager's epics."
            );
        }

        // Generate the document with all epics for this manager
        DocumentPayload documentPayload = epicAppService.buildManagerEpicsDocument(managerId);

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
    
    private void checkProjectPermission(Project project) {
        if (project == null) throw new BadRequestException("Project not found");
        User current = authService.getCurrentUser();
        User creator = project.getCreated_by();
        if (creator == null) throw new BadRequestException("Project creator not set");
        if (current != null && creator.getId().equals(current.getId())) return;
        if (current == null || current.getAccessLevel() == null || creator.getAccessLevel() == null || 
            current.getAccessLevel() < creator.getAccessLevel()) {
            throw new BadRequestException("Insufficient permission for workflow operation on this project");
        }
    }
}