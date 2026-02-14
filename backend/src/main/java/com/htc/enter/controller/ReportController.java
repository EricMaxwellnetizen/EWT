package com.htc.enter.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import com.htc.enter.model.Epic;
import com.htc.enter.model.Project;
import com.htc.enter.model.Story;
import com.htc.enter.model.User;
import com.htc.enter.repository.EpicRepository;
import com.htc.enter.repository.ProjectRepository;
import com.htc.enter.repository.StoryRepository;
import com.htc.enter.repository.UserRepository;
import com.htc.enter.service.ReportService;
import com.htc.enter.service.UserAuthService;
import com.htc.enter.util.DocumentPasswordUtil;

@RestController
@RequestMapping("/api/v1/report")
public class ReportController {

    private final ReportService reportService;
    private final ProjectRepository projectRepo;
    private final EpicRepository epicRepo;
    private final StoryRepository storyRepo;
    private final UserRepository userRepo;
    private final UserAuthService authService;

    public ReportController(ReportService reportService, ProjectRepository projectRepo, EpicRepository epicRepo, StoryRepository storyRepo, UserRepository userRepo, UserAuthService authService) {
        this.reportService = reportService;
        this.projectRepo = projectRepo;
        this.epicRepo = epicRepo;
        this.storyRepo = storyRepo;
        this.userRepo = userRepo;
        this.authService = authService;
    }

    @GetMapping("/projects/{status}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> projectsReport(@PathVariable String status, @RequestParam String type) throws Exception {
        User current = authService.getCurrentUser();
        if (current == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        String password = DocumentPasswordUtil.resolvePassword(current, null);
        
        List<Project> projects;
        if ("finished".equalsIgnoreCase(status)) {
            projects = projectRepo.findAll().stream().filter(Project::isIs_approved).collect(Collectors.toList());
        } else {
            projects = projectRepo.findAll().stream().filter(p -> !p.isIs_approved()).collect(Collectors.toList());
        }

        byte[] data;
        String filename;
        if ("excel".equalsIgnoreCase(type)) {
            data = reportService.exportProjectsExcel(projects);
            filename = "projects_" + status + ".xlsx";
        } else if ("word".equalsIgnoreCase(type)) {
            data = reportService.exportProjectsWord(projects, password);
            filename = "projects_" + status + ".docx";
        } else if ("pdf".equalsIgnoreCase(type)) {
            // PDF only for finished allowed per requirement
            if (!"finished".equalsIgnoreCase(status)) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            data = reportService.exportProjectsPdf(projects, password);
            filename = "projects_" + status + ".pdf";
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return buildFileResponse(data, filename);
    }

    @GetMapping("/epics/{status}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> epicsReport(@PathVariable String status, @RequestParam String type) throws Exception {
        User current = authService.getCurrentUser();
        if (current == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        String password = DocumentPasswordUtil.resolvePassword(current, null);
        
        List<Epic> epics;
        if ("finished".equalsIgnoreCase(status)) {
            epics = epicRepo.findAll().stream().filter(Epic::isIs_approved).collect(Collectors.toList());
        } else {
            epics = epicRepo.findAll().stream().filter(e -> !e.isIs_approved()).collect(Collectors.toList());
        }

        byte[] data;
        String filename;
        if ("excel".equalsIgnoreCase(type)) {
            data = reportService.exportEpicsExcel(epics);
            filename = "epics_" + status + ".xlsx";
        } else if ("word".equalsIgnoreCase(type)) {
            data = reportService.exportEpicsWord(epics, password);
            filename = "epics_" + status + ".docx";
        } else if ("pdf".equalsIgnoreCase(type)) {
            if (!"finished".equalsIgnoreCase(status)) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            data = reportService.exportEpicsPdf(epics, password);
            filename = "epics_" + status + ".pdf";
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return buildFileResponse(data, filename);
    }

    @GetMapping("/stories/{status}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> storiesReport(@PathVariable String status, @RequestParam String type) throws Exception {
        User current = authService.getCurrentUser();
        if (current == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        String password = DocumentPasswordUtil.resolvePassword(current, null);
        
        List<Story> stories;
        if ("finished".equalsIgnoreCase(status)) {
            stories = storyRepo.findAll().stream().filter(Story::isIs_approved).collect(Collectors.toList());
        } else {
            stories = storyRepo.findAll().stream().filter(s -> !s.isIs_approved()).collect(Collectors.toList());
        }

        byte[] data;
        String filename;
        if ("excel".equalsIgnoreCase(type)) {
            data = reportService.exportStoriesExcel(stories);
            filename = "stories_" + status + ".xlsx";
        } else if ("word".equalsIgnoreCase(type)) {
            data = reportService.exportStoriesWord(stories, password);
            filename = "stories_" + status + ".docx";
        } else if ("pdf".equalsIgnoreCase(type)) {
            if (!"finished".equalsIgnoreCase(status)) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            data = reportService.exportStoriesPdf(stories, password);
            filename = "stories_" + status + ".pdf";
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return buildFileResponse(data, filename);
    }

    private ResponseEntity<byte[]> buildFileResponse(byte[] data, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(data.length);
        if (filename.endsWith(".pdf")) headers.setContentType(MediaType.APPLICATION_PDF);
        else if (filename.endsWith(".docx")) headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        else headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
}