package com.htc.enter.service;

import java.io.IOException;

import com.htc.enter.dto.DocumentPayload;
import com.htc.enter.dto.ProjectDTO;
import com.htc.enter.model.Project;

public interface ProjectAppService {
    Project createProject(ProjectDTO dto);
    Project updateProject(Long id, ProjectDTO dto);
    void deleteProject(Long id);
    DocumentPayload buildManagerProjectsDocument(Long managerId) throws IOException;
}
