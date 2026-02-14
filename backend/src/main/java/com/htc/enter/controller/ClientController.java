package com.htc.enter.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.htc.enter.dto.ClientDTO;
import com.htc.enter.dto.ClientHierarchyDTO;
import com.htc.enter.dto.DocumentPayload;
import com.htc.enter.exception.BadRequestException;
import com.htc.enter.exception.ResourceNotFoundException;
import com.htc.enter.mapper.DomainMapper;
import com.htc.enter.model.Client;
import com.htc.enter.model.Epic;
import com.htc.enter.model.Project;
import com.htc.enter.model.Story;
import com.htc.enter.model.User;
import com.htc.enter.repository.EpicRepository;
import com.htc.enter.repository.ProjectRepository;
import com.htc.enter.repository.StoryRepository;
import com.htc.enter.service.ClientAppService;
import com.htc.enter.service.ClientService;
import com.htc.enter.service.UserAuthService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/client")
@Validated
public class ClientController {

    private final ClientService service;
    private final DomainMapper mapper;
    private final UserAuthService userAuthService;
    private final ClientAppService clientAppService;
    private final ProjectRepository projectRepository;
    private final EpicRepository epicRepository;
    private final StoryRepository storyRepository;

    public ClientController(ClientService service, DomainMapper mapper, 
                           UserAuthService userAuthService, ClientAppService clientAppService,
                           ProjectRepository projectRepository, EpicRepository epicRepository,
                           StoryRepository storyRepository) {
        this.service = service;
        this.mapper = mapper;
        this.userAuthService = userAuthService;
        this.clientAppService = clientAppService;
        this.projectRepository = projectRepository;
        this.epicRepository = epicRepository;
        this.storyRepository = storyRepository;
    }

    @GetMapping("/get")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ClientDTO>> getAll() {
        List<ClientDTO> list = service.findAll().stream().map(mapper::toClientDTO).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ClientDTO> getById(@PathVariable Long id) {
        Client c = service.findById(id);
        if (c == null) throw new ResourceNotFoundException("Client not found with id: " + id);
        return ResponseEntity.ok(mapper.toClientDTO(c));
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ClientDTO> create(@Valid @RequestBody ClientDTO dto) {
        if (dto == null) throw new BadRequestException("Client payload required");
        
        // Require access level 4+ for client creation (senior managers and above)
        if (!userAuthService.hasAccessLevel(4)) {
            throw new BadRequestException(
                "Insufficient access level. You need access level 4 or higher to create clients."
            );
        }
        
        Client saved = service.createFromDTO(dto);
        return new ResponseEntity<>(mapper.toClientDTO(saved), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ClientDTO> update(@PathVariable Long id, @Valid @RequestBody ClientDTO dto) {
        // Require access level 4+ for client updates
        if (!userAuthService.hasAccessLevel(4)) {
            throw new BadRequestException(
                "Insufficient access level. You need access level 4 or higher to update clients."
            );
        }
        
        Client saved = service.updateFromDTO(id, dto);
        return ResponseEntity.ok(mapper.toClientDTO(saved));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        // Require access level 4+ for client deletion
        if (!userAuthService.hasAccessLevel(4)) {
            throw new BadRequestException(
                "Insufficient access level. You need access level 4 or higher to delete clients."
            );
        }
        
        Client existing = service.findById(id);
        if (existing == null) throw new ResourceNotFoundException("Client not found with id: " + id);
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Download a Word document containing all clients
     * 
     * GET /api/v1/client/download
     * 
     * Requires: Authentication and access level 3+ (managers and admins)
     * 
     * Returns: Password-protected Word document with client details
     * Password format: {username}123
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/download")
    public void downloadClientsDocument(HttpServletResponse response) throws IOException {
        // Check if user has minimum access level 3
        if (!userAuthService.hasAccessLevel(3)) {
            throw new BadRequestException(
                "Access denied. You need access level 3 or higher to download client reports."
            );
        }

        // Generate the document with all clients
        DocumentPayload documentPayload = clientAppService.buildClientsDocument();

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
    
    /**
     * Get client with full hierarchy (projects -> epics -> stories -> users)
     * 
     * GET /api/v1/client/{id}/hierarchy
     * 
     * Requires: Authentication
     * 
     * Returns: Client with nested projects, epics, stories, and assigned users
     */
    @GetMapping("/{id}/hierarchy")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ClientHierarchyDTO> getClientHierarchy(@PathVariable Long id) {
        Client client = service.findById(id);
        if (client == null) {
            throw new ResourceNotFoundException("Client not found with id: " + id);
        }
        
        // Build the hierarchy DTO
        ClientHierarchyDTO dto = new ClientHierarchyDTO();
        dto.setId(Long.valueOf(client.getClient_id()));
        dto.setName(client.getName());
        dto.setEmail(client.getEmail());
        dto.setPhoneNumber(String.valueOf(client.getPhn_no())); // Convert long to String
        dto.setAddress(client.getAddress());
        dto.setCreatedAt(client.getCreatedAt());
        
        // Get all projects for this client
        List<Project> projects = projectRepository.findByClientId(Long.valueOf(client.getClient_id()));
        List<ClientHierarchyDTO.ProjectHierarchyDTO> projectDTOs = projects.stream().map(project -> {
            ClientHierarchyDTO.ProjectHierarchyDTO projectDTO = new ClientHierarchyDTO.ProjectHierarchyDTO();
            projectDTO.setId(project.getProjectId());
            projectDTO.setName(project.getName()); // Use getName() not getProjectName()
            projectDTO.setDescription(project.getDeliverables()); // deliverables field
            projectDTO.setDeadline(project.getDeadline() != null ? project.getDeadline().atStartOfDay() : null); // Convert LocalDate to LocalDateTime
            projectDTO.setIsApproved(project.isIs_approved());
            projectDTO.setProgress(null); // Project doesn't have progress field - calculate or set null
            
            // Set project manager
            if (project.getManager_id() != null) {
                User manager = project.getManager_id();
                ClientHierarchyDTO.UserBasicDTO managerDTO = new ClientHierarchyDTO.UserBasicDTO();
                managerDTO.setId(manager.getId());
                managerDTO.setFirstName(manager.getUsername()); // Use username as firstName
                managerDTO.setLastName(""); // No lastName field
                managerDTO.setEmail(manager.getEmail());
                managerDTO.setRole(manager.getRole());
                projectDTO.setManager(managerDTO);
            }
            
            // Get all epics for this project
            List<Epic> epics = epicRepository.findByProjectId(project.getProjectId());
            List<ClientHierarchyDTO.EpicHierarchyDTO> epicDTOs = epics.stream().map(epic -> {
                ClientHierarchyDTO.EpicHierarchyDTO epicDTO = new ClientHierarchyDTO.EpicHierarchyDTO();
                epicDTO.setId(epic.getEpicId());
                epicDTO.setName(epic.getName());
                epicDTO.setDescription(epic.getDeliverables()); // deliverables field
                epicDTO.setDueDate(epic.getDeadline() != null ? epic.getDeadline().atStartOfDay() : null); // Convert LocalDate to LocalDateTime
                epicDTO.setIsApproved(epic.isIs_approved());
                epicDTO.setProgress(null); // Epic doesn't have progress field - calculate or set null
                
                // Set epic manager
                if (epic.getManager_id() != null) {
                    User epicManager = epic.getManager_id();
                    ClientHierarchyDTO.UserBasicDTO epicManagerDTO = new ClientHierarchyDTO.UserBasicDTO();
                    epicManagerDTO.setId(epicManager.getId());
                    epicManagerDTO.setFirstName(epicManager.getUsername()); // Use username as firstName
                    epicManagerDTO.setLastName(""); // No lastName field
                    epicManagerDTO.setEmail(epicManager.getEmail());
                    epicManagerDTO.setRole(epicManager.getRole());
                    epicDTO.setManager(epicManagerDTO);
                }
                
                // Get all stories for this epic
                List<Story> stories = storyRepository.findByEpicId(epic.getEpicId());
                List<ClientHierarchyDTO.StoryHierarchyDTO> storyDTOs = stories.stream().map(story -> {
                    ClientHierarchyDTO.StoryHierarchyDTO storyDTO = new ClientHierarchyDTO.StoryHierarchyDTO();
                    storyDTO.setId(story.getStoryId());
                    storyDTO.setName(story.getTitle()); // Use title field
                    storyDTO.setDescription(story.getDeliverables()); // deliverables field
                    storyDTO.setStatus("TODO"); // Story doesn't have status field - set default or calculate
                    storyDTO.setDueDate(story.getDueDate() != null ? story.getDueDate().atStartOfDay() : null); // Convert LocalDate to LocalDateTime
                    storyDTO.setIsApproved(story.isIs_approved());
                    storyDTO.setEstimatedHours(story.getEstimatedHours());
                    storyDTO.setActualHours(story.getActualHours());
                    
                    // Set assigned user
                    if (story.getAssigned_to() != null) {
                        User assignedUser = story.getAssigned_to();
                        ClientHierarchyDTO.UserBasicDTO assignedDTO = new ClientHierarchyDTO.UserBasicDTO();
                        assignedDTO.setId(assignedUser.getId());
                        assignedDTO.setFirstName(assignedUser.getUsername()); // Use username as firstName
                        assignedDTO.setLastName(""); // No lastName field
                        assignedDTO.setEmail(assignedUser.getEmail());
                        assignedDTO.setRole(assignedUser.getRole());
                        storyDTO.setAssignedTo(assignedDTO);
                    }
                    
                    return storyDTO;
                }).toList(); // Use toList() instead of collect(Collectors.toList())
                
                epicDTO.setStories(storyDTOs);
                return epicDTO;
            }).toList();
            
            projectDTO.setEpics(epicDTOs);
            return projectDTO;
        }).toList();
        
        dto.setProjects(projectDTOs);
        return ResponseEntity.ok(dto);
    }
}