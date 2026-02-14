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
import com.htc.enter.dto.SlaRuleDTO;
import com.htc.enter.exception.BadRequestException;
import com.htc.enter.exception.ResourceNotFoundException;
import com.htc.enter.mapper.DomainMapper;
import com.htc.enter.model.SlaRule;
import com.htc.enter.model.User;
import com.htc.enter.repository.UserRepository;
import com.htc.enter.service.SlaRuleAppService;
import com.htc.enter.service.SlaRuleService;
import com.htc.enter.service.UserAuthService;

@RestController
@RequestMapping("/api/v1/sla")
@Validated
public class SlaRuleController {

    private final SlaRuleService service;
    private final DomainMapper mapper;
    private final UserAuthService authService;
    private final SlaRuleAppService slaRuleAppService;

    public SlaRuleController(SlaRuleService service, DomainMapper mapper, 
                            UserAuthService authService, SlaRuleAppService slaRuleAppService) {
        this.service = service;
        this.mapper = mapper;
        this.authService = authService;
        this.slaRuleAppService = slaRuleAppService;
    }

    @GetMapping("/get")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SlaRuleDTO>> getAll() {
        List<SlaRuleDTO> list = service.findAll().stream().map(mapper::toSlaRuleDTO).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SlaRuleDTO> getById(@PathVariable Long id) {
        SlaRule s = service.findById(id);
        if (s == null) throw new ResourceNotFoundException("SLA rule not found with id: " + id);
        return ResponseEntity.ok(mapper.toSlaRuleDTO(s));
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<SlaRuleDTO> create(@Valid @RequestBody SlaRuleDTO dto) {
        if (dto == null) throw new BadRequestException("SLA payload required");
        User current = authService.getCurrentUser();
        if (current == null || current.getAccessLevel() == null || current.getAccessLevel() < 4) {
            throw new BadRequestException("Only managers with access level 4 or above can create SLA rules");
        }
        SlaRule saved = service.createFromDTO(dto);
        return new ResponseEntity<>(mapper.toSlaRuleDTO(saved), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<SlaRuleDTO> update(@PathVariable Long id, @Valid @RequestBody SlaRuleDTO dto) {
        User current = authService.getCurrentUser();
        if (current == null || current.getAccessLevel() == null || current.getAccessLevel() < 4) {
            throw new BadRequestException("Only managers with access level 4 or above can update SLA rules");
        }
        SlaRule saved = service.updateFromDTO(id, dto);
        return ResponseEntity.ok(mapper.toSlaRuleDTO(saved));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        User current = authService.getCurrentUser();
        if (current == null || current.getAccessLevel() == null || current.getAccessLevel() < 4) {
            throw new BadRequestException("Only managers with access level 4 or above can delete SLA rules");
        }
        SlaRule existing = service.findById(id);
        if (existing == null) throw new ResourceNotFoundException("SLA rule not found with id: " + id);
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Download a Word document containing all SLA rules
     * 
     * GET /api/v1/sla/download
     * 
     * Requires: Authentication and access level 3+ (managers and admins)
     * 
     * Returns: Password-protected Word document with SLA rule details
     * Password format: {username}123
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/download")
    public void downloadSlaRulesDocument(HttpServletResponse response) throws IOException {
        // Check if user has minimum access level 3
        if (!authService.hasAccessLevel(3)) {
            throw new BadRequestException(
                "Access denied. You need access level 3 or higher to download SLA rule reports."
            );
        }

        // Generate the document with all SLA rules
        DocumentPayload documentPayload = slaRuleAppService.buildSlaRulesDocument();

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