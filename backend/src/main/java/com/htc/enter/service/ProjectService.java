package com.htc.enter.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.htc.enter.model.Manager;
import com.htc.enter.model.Project;
import com.htc.enter.dto.ProjectDTO;

public interface ProjectService {
	Project save(Project project);
	Project findById(Long id);
    List<Project> findAll();
    Page<Project> findAll(Pageable pageable);
    void deleteById(Long id);

    // create/update from DTO (resolve relations)
    Project createFromDTO(ProjectDTO dto);
    Project updateFromDTO(Long id, ProjectDTO dto);

    // find projects for a manager
    List<Project> findByManagerId(Long managerId);
    Page<Project> findByManagerId(Long managerId, Pageable pageable);
}