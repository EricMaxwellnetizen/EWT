package com.htc.enter.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.security.access.prepost.PreAuthorize;

import com.htc.enter.dto.*;
import com.htc.enter.exception.BadRequestException;
import com.htc.enter.exception.ForbiddenException;
import com.htc.enter.exception.ResourceNotFoundException;
import com.htc.enter.mapper.UserMapper;
import com.htc.enter.model.*;
import com.htc.enter.service.UserAppService;
import com.htc.enter.service.UserService;
import com.htc.enter.service.FileStorageService;
import com.htc.enter.util.AccessControlUtil;

@RestController
@RequestMapping("/api/v1/user")
@Validated
public class UserController {

    private final UserService service;
    private final UserMapper mapper;
    private final UserAppService userAppService;
    private final AccessControlUtil accessControlUtil;
    private final FileStorageService fileStorageService;

    public UserController(UserService service, UserMapper mapper, 
                         UserAppService userAppService,
                         AccessControlUtil accessControlUtil, FileStorageService fileStorageService) {
        this.service = service;
        this.mapper = mapper;
        this.userAppService = userAppService;
        this.accessControlUtil = accessControlUtil;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/get")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserOutput>> getAllUsers() {
        List<UserOutput> users = service.findAll()
                .stream()
                .map(this::toOutput)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/paginated")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<UserOutput>> getAllUsersPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<UserOutput> userPage = service.findAll(pageable)
            .map(this::toOutput);
        
        return ResponseEntity.ok(userPage);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserOutput> getUserById(@PathVariable Long id) {
        User user = service.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return ResponseEntity.ok(toOutput(user));
    }
    
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserOutput> getCurrentUserProfile() {
        User currentUser = accessControlUtil.getCurrentUser();
        return ResponseEntity.ok(toOutput(currentUser));
    }
    
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserOutput> updateOwnProfile(@Valid @RequestBody UserInput input) {
        User currentUser = accessControlUtil.getCurrentUser();
        
        if (input == null)
            throw new BadRequestException("User payload is required");

        User updated = service.updateFromInput(currentUser.getId(), input);
        return ResponseEntity.ok(toOutput(updated));
    }
    
    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> changeOwnPassword(
            @RequestBody Map<String, String> passwordRequest) {
        
        String oldPassword = passwordRequest.get("oldPassword");
        String newPassword = passwordRequest.get("newPassword");
        
        if (oldPassword == null || oldPassword.isBlank())
            throw new BadRequestException("Old password is required");
        
        if (newPassword == null || newPassword.isBlank())
            throw new BadRequestException("New password is required");
        
        if (newPassword.length() < 6)
            throw new BadRequestException("New password must be at least 6 characters");
            
        User currentUser = accessControlUtil.getCurrentUser();
        service.changePassword(currentUser.getId(), oldPassword, newPassword);
        
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
    
    @GetMapping("/editable")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserOutput>> getEditableUsers() {
        User currentUser = accessControlUtil.getCurrentUser();
        
        List<UserOutput> editableUsers = service.findAll()
                .stream()
                .filter(user -> {
                    try {
                        accessControlUtil.checkEditPermission(currentUser, user);
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .map(this::toOutput)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(editableUsers);
    }
    
    @GetMapping("/can-edit/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> checkCanEditUser(@PathVariable Long userId) {
        User currentUser = accessControlUtil.getCurrentUser();
        User targetUser = service.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        try {
            accessControlUtil.checkEditPermission(currentUser, targetUser);
            return ResponseEntity.ok(Map.of(
                "canEdit", true,
                "reason", "You have permission to edit this user"
            ));
        } catch (ForbiddenException e) {
            return ResponseEntity.ok(Map.of(
                "canEdit", false,
                "reason", e.getMessage()
            ));
        }
    }

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserOutput> createUser(@Valid @RequestBody UserInput input) {

        if (input == null)
            throw new BadRequestException("User payload is required");

        if (input.getUsername() == null || input.getUsername().isBlank())
            throw new BadRequestException("Username is required");

        if (input.getPassword() == null || input.getPassword().isBlank())
            throw new BadRequestException("Password is required");

        // Validate email domain - only @htcinc.com emails allowed
        if (input.getEmail() == null || input.getEmail().isBlank())
            throw new BadRequestException("Email is required");
        
        if (!input.getEmail().toLowerCase().endsWith("@htcinc.com"))
            throw new BadRequestException("Only @htcinc.com email addresses are allowed");

        // Get current user for access level validation
        User currentUser = accessControlUtil.getCurrentUser();
        User saved = service.createFromInput(input, currentUser);
        return new ResponseEntity<>(toOutput(saved), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserOutput> updateUser(@PathVariable Long id,
                                                 @Valid @RequestBody UserInput input) {

        User targetUser = service.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Get current user and check if they have permission to edit
        User currentUser = accessControlUtil.getCurrentUser();
        accessControlUtil.checkEditPermission(currentUser, targetUser);

        if (input == null)
            throw new BadRequestException("User payload is required");

        User updated = service.updateFromInput(id, input);
        return ResponseEntity.ok(toOutput(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated() and hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        service.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Upload profile picture for current user
     * 
     * POST /api/v1/user/me/profile-picture
     */
    @PostMapping("/me/profile-picture")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> uploadProfilePicture(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                throw new BadRequestException("File is empty");
            }
            
            if (file.getSize() > 5 * 1024 * 1024) { // 5MB limit
                throw new BadRequestException("File size exceeds 5MB limit");
            }
            
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new BadRequestException("Only image files are allowed");
            }

            // Store file with a user-specific filename
            User currentUser = accessControlUtil.getCurrentUser();
            String fileName = "profile_" + currentUser.getId() + "_" + System.currentTimeMillis() + 
                            getFileExtension(file.getOriginalFilename());
            
            String storedFileName = fileStorageService.storeFile(file, fileName);
            
            // Update user's profile picture URL
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/v1/files/download/")
                    .path(storedFileName)
                    .toUriString();
            
            currentUser.setProfilePictureUrl(fileDownloadUri);
            service.save(currentUser);

            Map<String, String> response = new java.util.HashMap<>();
            response.put("fileName", storedFileName);
            response.put("fileDownloadUri", fileDownloadUri);
            response.put("message", "Profile picture uploaded successfully");

            return ResponseEntity.ok(response);
        } catch (IOException ex) {
            throw new BadRequestException("Failed to upload profile picture: " + ex.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null) return ".jpg";
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot) : ".jpg";
    }
    
    /**
     * Download a Word document containing all users
     * 
     * GET /api/v1/user/download
     * 
     * Requires: Authentication - accessible by all authenticated users
     * 
     * Returns: Password-protected Word document with user details
     * Password format: {username}123
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/download")
    public void downloadUsersDocument(HttpServletResponse response) throws IOException {
        // Generate the document with all users (sensitive fields excluded for non-admins)
        DocumentPayload documentPayload = userAppService.buildUsersDocument();

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
    
    private UserOutput toOutput(User user) {
        if (user instanceof Admin) return mapper.toAdminOutput((Admin) user);
        if (user instanceof Employee) return mapper.toEmployeeOutput((Employee) user);
        if (user instanceof Manager) return mapper.toManagerOutput((Manager) user);
        return mapper.toUserOutput(user);
    }
}