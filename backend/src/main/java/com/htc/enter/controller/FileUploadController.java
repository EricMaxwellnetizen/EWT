package com.htc.enter.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.htc.enter.service.FileStorageService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/files")
public class FileUploadController {

    private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);
    
    private final FileStorageService fileStorageService;

    public FileUploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = fileStorageService.storeFile(file);
            
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/v1/files/download/")
                    .path(fileName)
                    .toUriString();

            Map<String, String> response = new HashMap<>();
            response.put("fileName", fileName);
            response.put("fileDownloadUri", fileDownloadUri);
            response.put("fileType", file.getContentType());
            response.put("size", String.valueOf(file.getSize()));

            return ResponseEntity.ok(response);
        } catch (IOException ex) {
            log.error("Failed to upload file", ex);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Could not upload file: " + ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/upload/project/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> uploadFileForProject(
            @PathVariable Long projectId,
            @RequestParam("file") MultipartFile file) {
        try {
            String filePath = fileStorageService.storeFileForProject(file, projectId);
            
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/v1/files/download-path/")
                    .path(filePath)
                    .toUriString();

            Map<String, String> response = new HashMap<>();
            response.put("fileName", file.getOriginalFilename());
            response.put("filePath", filePath);
            response.put("fileDownloadUri", fileDownloadUri);
            response.put("fileType", file.getContentType());
            response.put("size", String.valueOf(file.getSize()));
            response.put("projectId", String.valueOf(projectId));

            return ResponseEntity.ok(response);
        } catch (IOException ex) {
            log.error("Failed to upload file for project " + projectId, ex);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Could not upload file: " + ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/upload/epic/{projectId}/{epicId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> uploadFileForEpic(
            @PathVariable Long projectId,
            @PathVariable Long epicId,
            @RequestParam("file") MultipartFile file) {
        try {
            String filePath = fileStorageService.storeFileForEpic(file, projectId, epicId);
            
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/v1/files/download-path/")
                    .path(filePath)
                    .toUriString();

            Map<String, String> response = new HashMap<>();
            response.put("fileName", file.getOriginalFilename());
            response.put("filePath", filePath);
            response.put("fileDownloadUri", fileDownloadUri);
            response.put("fileType", file.getContentType());
            response.put("size", String.valueOf(file.getSize()));
            response.put("projectId", String.valueOf(projectId));
            response.put("epicId", String.valueOf(epicId));

            return ResponseEntity.ok(response);
        } catch (IOException ex) {
            log.error("Failed to upload file for epic " + epicId, ex);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Could not upload file: " + ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/upload/story/{projectId}/{storyId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> uploadFileForStory(
            @PathVariable Long projectId,
            @PathVariable Long storyId,
            @RequestParam(value = "epicId", required = false) Long epicId,
            @RequestParam("file") MultipartFile file) {
        try {
            String filePath = fileStorageService.storeFileForStory(file, projectId, epicId, storyId);
            
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/v1/files/download-path/")
                    .path(filePath)
                    .toUriString();

            Map<String, String> response = new HashMap<>();
            response.put("fileName", file.getOriginalFilename());
            response.put("filePath", filePath);
            response.put("fileDownloadUri", fileDownloadUri);
            response.put("fileType", file.getContentType());
            response.put("size", String.valueOf(file.getSize()));
            response.put("projectId", String.valueOf(projectId));
            response.put("storyId", String.valueOf(storyId));
            if (epicId != null) {
                response.put("epicId", String.valueOf(epicId));
            }

            return ResponseEntity.ok(response);
        } catch (IOException ex) {
            log.error("Failed to upload file for story " + storyId, ex);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Could not upload file: " + ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/list/project/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> listFilesForProject(@PathVariable Long projectId) {
        try {
            List<String> files = fileStorageService.listFilesForProject(projectId);
            Map<String, Object> response = new HashMap<>();
            response.put("projectId", projectId);
            response.put("files", files);
            response.put("count", files.size());
            return ResponseEntity.ok(response);
        } catch (IOException ex) {
            log.error("Failed to list files for project " + projectId, ex);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Could not list files: " + ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/list/epic/{projectId}/{epicId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> listFilesForEpic(
            @PathVariable Long projectId,
            @PathVariable Long epicId) {
        try {
            List<String> files = fileStorageService.listFilesForEpic(projectId, epicId);
            Map<String, Object> response = new HashMap<>();
            response.put("projectId", projectId);
            response.put("epicId", epicId);
            response.put("files", files);
            response.put("count", files.size());
            return ResponseEntity.ok(response);
        } catch (IOException ex) {
            log.error("Failed to list files for epic " + epicId, ex);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Could not list files: " + ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/list/story/{projectId}/{storyId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> listFilesForStory(
            @PathVariable Long projectId,
            @PathVariable Long storyId,
            @RequestParam(value = "epicId", required = false) Long epicId) {
        try {
            List<String> files = fileStorageService.listFilesForStory(projectId, epicId, storyId);
            Map<String, Object> response = new HashMap<>();
            response.put("projectId", projectId);
            response.put("storyId", storyId);
            if (epicId != null) {
                response.put("epicId", epicId);
            }
            response.put("files", files);
            response.put("count", files.size());
            return ResponseEntity.ok(response);
        } catch (IOException ex) {
            log.error("Failed to list files for story " + storyId, ex);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Could not list files: " + ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/upload-multiple")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        Map<String, Object> response = new HashMap<>();
        java.util.List<Map<String, String>> uploadedFiles = new java.util.ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                String fileName = fileStorageService.storeFile(file);
                String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/api/v1/files/download/")
                        .path(fileName)
                        .toUriString();

                Map<String, String> fileInfo = new HashMap<>();
                fileInfo.put("fileName", fileName);
                fileInfo.put("fileDownloadUri", fileDownloadUri);
                fileInfo.put("fileType", file.getContentType());
                fileInfo.put("size", String.valueOf(file.getSize()));
                
                uploadedFiles.add(fileInfo);
            } catch (IOException ex) {
                log.error("Failed to upload file: " + file.getOriginalFilename(), ex);
            }
        }
        
        response.put("files", uploadedFiles);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/download/{fileName:.+}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        try {
            Resource resource = fileStorageService.loadFileAsResource(fileName);

            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                log.info("Could not determine file type.");
            }

            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (IOException ex) {
            log.error("File not found: " + fileName, ex);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/download-path/**")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadFileByPath(HttpServletRequest request) {
        try {
            // Extract the full path after /download-path/
            String fullPath = request.getRequestURI().split("/download-path/")[1];
            Resource resource = fileStorageService.loadFileAsResource(fullPath);

            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                log.info("Could not determine file type.");
            }

            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception ex) {
            log.error("File not found", ex);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete/{fileName:.+}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<Map<String, String>> deleteFile(@PathVariable String fileName) {
        try {
            fileStorageService.deleteFile(fileName);
            Map<String, String> response = new HashMap<>();
            response.put("message", "File deleted successfully: " + fileName);
            return ResponseEntity.ok(response);
        } catch (IOException ex) {
            log.error("Failed to delete file: " + fileName, ex);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Could not delete file: " + ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/delete-path/**")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<Map<String, String>> deleteFileByPath(HttpServletRequest request) {
        try {
            String fullPath = request.getRequestURI().split("/delete-path/")[1];
            fileStorageService.deleteFile(fullPath);
            Map<String, String> response = new HashMap<>();
            response.put("message", "File deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Failed to delete file", ex);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Could not delete file: " + ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
