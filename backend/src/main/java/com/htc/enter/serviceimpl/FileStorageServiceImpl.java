package com.htc.enter.serviceimpl;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.htc.enter.service.FileStorageService;

import jakarta.annotation.PostConstruct;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageServiceImpl(@Value("${file.upload-dir:uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    @Override
    public void init() throws IOException {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new IOException("Could not create upload directory!", ex);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String storeFile(MultipartFile file) throws IOException {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString() + "_" + originalFileName;
        return storeFile(file, fileName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String storeFile(MultipartFile file, String customFileName) throws IOException {
        try {
            // Check if the file's name contains invalid characters
            if (customFileName.contains("..")) {
                throw new IOException("Filename contains invalid path sequence: " + customFileName);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(customFileName);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            return customFileName;
        } catch (IOException ex) {
            throw new IOException("Could not store file " + customFileName, ex);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String storeFileForProject(MultipartFile file, Long projectId) throws IOException {
        String relativePath = "projects/project_" + projectId;
        return storeFileInPath(file, relativePath);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String storeFileForEpic(MultipartFile file, Long projectId, Long epicId) throws IOException {
        String relativePath = "projects/project_" + projectId + "/epics/epic_" + epicId;
        return storeFileInPath(file, relativePath);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String storeFileForStory(MultipartFile file, Long projectId, Long epicId, Long storyId) throws IOException {
        String relativePath;
        if (epicId != null) {
            relativePath = "projects/project_" + projectId + "/epics/epic_" + epicId + "/stories/story_" + storyId;
        } else {
            relativePath = "projects/project_" + projectId + "/stories/story_" + storyId;
        }
        return storeFileInPath(file, relativePath);
    }

    private String storeFileInPath(MultipartFile file, String relativePath) throws IOException {
        try {
            // Check if the file's name contains invalid characters
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
            if (originalFileName.contains("..")) {
                throw new IOException("Filename contains invalid path sequence: " + originalFileName);
            }

            // Create directory if it doesn't exist
            Path directoryPath = this.fileStorageLocation.resolve(relativePath);
            Files.createDirectories(directoryPath);

            // Generate unique filename
            String fileName = UUID.randomUUID().toString() + "_" + originalFileName;
            String fullRelativePath = relativePath + "/" + fileName;

            // Copy file to the target location
            Path targetLocation = this.fileStorageLocation.resolve(fullRelativePath);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            return fullRelativePath;
        } catch (IOException ex) {
            throw new IOException("Could not store file in path: " + relativePath, ex);
        }
    }

    @Override
    public Resource loadFileAsResource(String fileName) throws IOException {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new IOException("File not found: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new IOException("File not found: " + fileName, ex);
        }
    }

    @Override
    public Resource loadFileAsResource(String relativePath, String fileName) throws IOException {
        String fullPath = relativePath + "/" + fileName;
        return loadFileAsResource(fullPath);
    }

    @Override
    public void deleteFile(String fileName) throws IOException {
        Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
        Files.deleteIfExists(filePath);
    }

    @Override
    public void deleteFile(String relativePath, String fileName) throws IOException {
        String fullPath = relativePath + "/" + fileName;
        deleteFile(fullPath);
    }

    @Override
    public List<String> listFilesForProject(Long projectId) throws IOException {
        String relativePath = "projects/project_" + projectId;
        return listFilesInPath(relativePath);
    }

    @Override
    public List<String> listFilesForEpic(Long projectId, Long epicId) throws IOException {
        String relativePath = "projects/project_" + projectId + "/epics/epic_" + epicId;
        return listFilesInPath(relativePath);
    }

    @Override
    public List<String> listFilesForStory(Long projectId, Long epicId, Long storyId) throws IOException {
        String relativePath;
        if (epicId != null) {
            relativePath = "projects/project_" + projectId + "/epics/epic_" + epicId + "/stories/story_" + storyId;
        } else {
            relativePath = "projects/project_" + projectId + "/stories/story_" + storyId;
        }
        return listFilesInPath(relativePath);
    }

    private List<String> listFilesInPath(String relativePath) throws IOException {
        Path directoryPath = this.fileStorageLocation.resolve(relativePath);
        
        if (!Files.exists(directoryPath)) {
            return new ArrayList<>();
        }

        try (Stream<Path> paths = Files.walk(directoryPath, 1)) {
            return paths
                .filter(Files::isRegularFile)
                .map(path -> path.getFileName().toString())
                .collect(Collectors.toList());
        } catch (IOException ex) {
            throw new IOException("Could not list files in path: " + relativePath, ex);
        }
    }

    @Override
    public Path getFilePath(String fileName) {
        return this.fileStorageLocation.resolve(fileName).normalize();
    }
}
