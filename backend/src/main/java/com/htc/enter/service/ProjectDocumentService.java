package com.htc.enter.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import com.htc.enter.model.Project;

public interface ProjectDocumentService {
    
    ByteArrayOutputStream generateManagerProjectsDocument(Long managerId, List<Project> projects) throws IOException;
    
    ByteArrayOutputStream generatePasswordProtectedProjectDocument(Project project, String password) throws IOException;
    
    ByteArrayOutputStream generatePasswordProtectedManagerProjectsDocument(Long managerId, List<Project> projects, String password) throws IOException;
}