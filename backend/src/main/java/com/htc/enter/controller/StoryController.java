package com.htc.enter.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
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
import com.htc.enter.dto.StoryDTO;
import com.htc.enter.exception.BadRequestException;
import com.htc.enter.exception.ResourceNotFoundException;
import com.htc.enter.mapper.DomainMapper;
import com.htc.enter.model.Project;
import com.htc.enter.model.Story;
import com.htc.enter.model.User;
import com.htc.enter.repository.ProjectRepository;
import com.htc.enter.repository.StoryRepository;
import com.htc.enter.service.StoryAppService;
import com.htc.enter.service.StoryService;
import com.htc.enter.service.UserAuthService;

@RestController
@RequestMapping("/api/v1/story")
@Validated
public class StoryController {

    private final StoryService service;
    private final DomainMapper mapper;
    private final ProjectRepository projectRepo;
    private final StoryRepository storyRepo;
    private final UserAuthService authService;
    private final StoryAppService storyAppService;

    public StoryController(StoryService service, DomainMapper mapper, ProjectRepository projectRepo, 
                          StoryRepository storyRepo, UserAuthService authService,
                          StoryAppService storyAppService) {
        this.service = service;
        this.mapper = mapper;
        this.projectRepo = projectRepo;
        this.storyRepo = storyRepo;
        this.authService = authService;
        this.storyAppService = storyAppService;
    }

    @GetMapping("/get")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<StoryDTO>> getAll() {
        // Access level 2+ can view stories
        if (!authService.hasAccessLevel(2)) {
            throw new BadRequestException("Insufficient access level to view stories.");
        }
        List<StoryDTO> list = service.findAll().stream().map(mapper::toTaskDTO).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StoryDTO> getById(@PathVariable Long id) {
        // Access level 2+ can view stories
        if (!authService.hasAccessLevel(2)) {
            throw new BadRequestException("Insufficient access level to view stories.");
        }
        Story s = service.findById(id);
        if (s == null) throw new ResourceNotFoundException("Story not found with id: " + id);
        return ResponseEntity.ok(mapper.toTaskDTO(s));
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<StoryDTO> create(@Valid @RequestBody StoryDTO dto) {
        if (dto == null) throw new BadRequestException("Story payload required");
        
        // Require access level 2+ (employees and above can create stories)
        if (!authService.hasAccessLevel(2)) {
            throw new BadRequestException(
                "Insufficient access level. You need access level 2 or higher to create stories."
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
        
        // Level 2: Can only assign stories to themselves
        if (accessLevel != null && accessLevel == 2) {
            if (dto.getAssignedToId() != null && !dto.getAssignedToId().equals(currentUser.getId())) {
                throw new BadRequestException(
                    "Employees with access level 2 can only create stories assigned to themselves."
                );
            }
        }
        
        // Level 3: Can assign to themselves or their direct reports
        else if (accessLevel != null && accessLevel == 3) {
            if (dto.getAssignedToId() != null) {
                Long targetUserId = dto.getAssignedToId();
                if (!targetUserId.equals(currentUser.getId()) && 
                    !authService.isDirectReport(targetUserId, currentUser.getId())) {
                    throw new BadRequestException(
                        "Managers can only create stories for themselves or their direct reports."
                    );
                }
            }
        }
        
        // Level 4+: Can assign to anyone
        
        Story created = service.createFromDTO(dto);
        return new ResponseEntity<>(mapper.toTaskDTO(created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<StoryDTO> update(@PathVariable Long id, @Valid @RequestBody StoryDTO dto) {
        Story existing = service.findById(id);
        if (existing == null) throw new ResourceNotFoundException("Story not found with id: " + id);

        // Require access level 2+ (employees and above)
        if (!authService.hasAccessLevel(2)) {
            throw new BadRequestException(
                "Insufficient access level. You need access level 2 or higher to update stories."
            );
        }

        User current = authService.getCurrentUser();
        if (current == null) throw new BadRequestException("Authentication required");

        User creator = existing.getCreated_by();
        if (creator == null) {
            creator = existing.getManager();
        }
        boolean creatorIsSenior = creator != null && creator.getAccessLevel() != null && creator.getAccessLevel() >= 4;
        boolean isApproved = existing.isIs_approved();

        if (!isApproved && !creatorIsSenior) {
            boolean modifiesWorkFields = dto.getDeliverables() != null
                    || dto.getDescription() != null
                    || dto.getIsEnd() != null;
            if (modifiesWorkFields) {
                throw new BadRequestException(
                    "Story cannot be worked on until approved by the creator's reporting manager."
                );
            }
        }

        // Check approval privileges: reporting manager approves unless creator is senior
        if (dto.getIsApproved() != null && dto.getIsApproved()) {
            if (creator != null && creatorIsSenior && creator.getId().equals(current.getId())) {
                // creator auto-approves
            } else if (creator != null && creator.getReportingTo() != null
                    && creator.getReportingTo().getId().equals(current.getId())) {
                // reporting manager approves
            } else {
                throw new BadRequestException("Only the creator's reporting manager can approve this story");
            }
            existing.setIs_approved(true);
            if (existing.getIs_end() == null) existing.setIs_end(LocalDate.now());
        }

        // Other modifications allowed only for creating manager or admins
        if (!current.getId().equals(existing.getManager() != null ? existing.getManager().getId() : null)) {
            if (current.getAccessLevel() == null || current.getAccessLevel() < 4) {
                throw new BadRequestException("Only the creating manager or senior manager/admin can modify this story");
            }
        }

        Story saved = service.updateFromDTO(id, dto);
        return ResponseEntity.ok(mapper.toTaskDTO(saved));
    }

    /**
     * Mark a story as completed.
     * Cascades automatically: if all stories in the epic are done, the epic completes.
     * If all epics in the project are done, the project completes.
     */
    @PatchMapping("/{id}/complete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StoryDTO> completeStory(@PathVariable Long id) {
        if (!authService.hasAccessLevel(2)) {
            throw new BadRequestException("Insufficient access level to complete stories.");
        }
        Story completed = service.completeStory(id);
        return ResponseEntity.ok(mapper.toTaskDTO(completed));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Story existing = service.findById(id);
        if (existing == null) throw new ResourceNotFoundException("Story not found with id: " + id);

        // Require access level 2+ (employees and above)
        if (!authService.hasAccessLevel(2)) {
            throw new BadRequestException(
                "Insufficient access level. You need access level 2 or higher to delete stories."
            );
        }

        User current = authService.getCurrentUser();
        if (current == null) throw new BadRequestException("Authentication required");

        if (existing.getManager() == null || !existing.getManager().getId().equals(current.getId())) {
            if (current.getAccessLevel() == null || current.getAccessLevel() < 4) {
                throw new BadRequestException("Only the creating manager or senior manager/admin can delete this story");
            }
        }
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Download a Word document containing all stories for a specific manager
     * 
     * GET /api/v1/story/manager/{managerId}/download
     * 
     * Requires: Authentication and access level 2+ (employees and above)
     * 
     * Returns: Password-protected Word document with story details
     * Password format: {username}123
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/manager/{managerId}/download")
    public void downloadManagerStoriesDocument(
            @PathVariable Long managerId, 
            HttpServletResponse response) throws IOException {
        
        // Check if user has minimum access level 2
        if (!authService.hasAccessLevel(2)) {
            throw new BadRequestException(
                "Access denied. You need access level 2 or higher to download story reports."
            );
        }
        
        // Check if user has permission to access this manager's resources
        if (!authService.canAccessManagerResources(managerId, 2)) {
            throw new BadRequestException(
                "Access denied. You don't have permission to view this manager's stories."
            );
        }

        // Generate the document with all stories for this manager
        DocumentPayload documentPayload = storyAppService.buildManagerStoriesDocument(managerId);

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