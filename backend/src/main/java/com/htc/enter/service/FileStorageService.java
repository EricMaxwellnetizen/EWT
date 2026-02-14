package com.htc.enter.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    
    /**
     * Store uploaded file and return the stored filename
     */
    String storeFile(MultipartFile file) throws IOException;
    
    /**
     * Store file with custom filename
     */
    String storeFile(MultipartFile file, String customFileName) throws IOException;
    
    /**
     * Store file for a specific project
     * @param file The file to upload
     * @param projectId The project ID
     * @return The relative path of the stored file
     */
    String storeFileForProject(MultipartFile file, Long projectId) throws IOException;
    
    /**
     * Store file for a specific epic
     * @param file The file to upload
     * @param projectId The project ID
     * @param epicId The epic ID
     * @return The relative path of the stored file
     */
    String storeFileForEpic(MultipartFile file, Long projectId, Long epicId) throws IOException;
    
    /**
     * Store file for a specific story
     * @param file The file to upload
     * @param projectId The project ID
     * @param epicId The epic ID (can be null)
     * @param storyId The story ID
     * @return The relative path of the stored file
     */
    String storeFileForStory(MultipartFile file, Long projectId, Long epicId, Long storyId) throws IOException;
    
    /**
     * Load file as Resource
     */
    Resource loadFileAsResource(String fileName) throws IOException;
    
    /**
     * Load file as Resource from specific path
     */
    Resource loadFileAsResource(String relativePath, String fileName) throws IOException;
    
    /**
     * Delete file
     */
    void deleteFile(String fileName) throws IOException;
    
    /**
     * Delete file from specific path
     */
    void deleteFile(String relativePath, String fileName) throws IOException;
    
    /**
     * List all files for a project
     */
    List<String> listFilesForProject(Long projectId) throws IOException;
    
    /**
     * List all files for an epic
     */
    List<String> listFilesForEpic(Long projectId, Long epicId) throws IOException;
    
    /**
     * List all files for a story
     */
    List<String> listFilesForStory(Long projectId, Long epicId, Long storyId) throws IOException;
    
    /**
     * Get file path
     */
    Path getFilePath(String fileName);
    
    /**
     * Initialize storage location
     */
    void init() throws IOException;
}
