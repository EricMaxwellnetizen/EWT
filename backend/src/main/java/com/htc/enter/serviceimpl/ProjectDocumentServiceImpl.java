package com.htc.enter.serviceimpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.htc.enter.model.Project;
import com.htc.enter.service.ProjectDocumentService;

@Service
public class ProjectDocumentServiceImpl extends BaseDocumentService implements ProjectDocumentService {

    @Override
    @Transactional(readOnly = true)
    public ByteArrayOutputStream generateManagerProjectsDocument(Long managerId, List<Project> projects) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (XWPFDocument doc = new XWPFDocument()) {
            writeProjectsDocument(doc,
                    "Projects for Manager ID: " + managerId,
                    "No projects found for this manager.",
                    projects,
                    true);
            doc.write(outputStream);
        }

        return outputStream;
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayOutputStream generatePasswordProtectedProjectDocument(Project project, String password) throws IOException {
        ByteArrayOutputStream tempStream = new ByteArrayOutputStream();
        try (XWPFDocument doc = new XWPFDocument()) {
            addHeaderWithLogo(doc);
            createDocumentTitle(doc, "Project Created: " + project.getName());
            addProjectContent(doc, project);
            doc.write(tempStream);
        }

        return encryptDoc(tempStream, password);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ByteArrayOutputStream generatePasswordProtectedManagerProjectsDocument
    (Long managerId, List<Project> projects, String password) throws IOException {
        ByteArrayOutputStream tempStream = new ByteArrayOutputStream();
        try (XWPFDocument doc = new XWPFDocument()) {
            writeProjectsDocument(doc,
                    "Projects for Manager ID: " + managerId,
                    "No projects found for this manager.",
                    projects,
                    false);
            doc.write(tempStream);
        }
        return encryptDoc(tempStream, password);
    }

    private void addProjectContent(XWPFDocument doc, Project project) {
        XWPFParagraph p = createSectionHeader(doc,
                "Project: " + project.getName() + " (id=" + project.getProjectId() + ")");
        
        XWPFRun r2 = p.createRun();
        addField(r2, "ClientId", project.getClient_id() != null ? project.getClient_id().getClient_id() : null);
        addField(r2, "Manager", project.getManager_id() != null ? 
            project.getManager_id().getUsername() + " (id=" + project.getManager_id().getId() + ")" : null);
        addField(r2, "Created By", project.getCreated_by() != null ? project.getCreated_by().getUsername() : null);
        addField(r2, "Deliverables", project.getDeliverables());
        addField(r2, "Deadline", project.getDeadline());
        addField(r2, "Approved", project.isIs_approved());
        addField(r2, "Ended", project.getIs_end());
        addSectionSpacing(r2);
    }

    private void writeProjectsDocument(
            XWPFDocument doc,
            String titleText,
            String emptyMessage,
            List<Project> projects,
            boolean includeHeader) {
        if (includeHeader) {
            addHeaderWithLogo(doc);
        }
        createDocumentTitle(doc, titleText);

        if (projects == null || projects.isEmpty()) {
            addEmptyMessage(doc, emptyMessage);
        } else {
            for (Project pr : projects) {
                addProjectContent(doc, pr);
            }
        }
    }

    private void addHeaderWithLogo(XWPFDocument doc) {
        try {
            ClassPathResource logoResource = new ClassPathResource("static/logo.png");
            if (logoResource.exists()) {
                try (InputStream is = logoResource.getInputStream()) {
                    // Apache POI doesn't directly support header images in simple API; add as first paragraph.
                    XWPFParagraph header = doc.createParagraph();
                    header.setAlignment(ParagraphAlignment.CENTER);
                    XWPFRun imgRun = header.createRun();
                    imgRun.setText("HTC Global Services");
                    imgRun.addBreak();
                    // If desired, we could embed the image using addPicture, but it requires picture type and dimensions.
                    // Leaving text placeholder plus optional future enhancement.
                }
            }
        } catch (Exception ignored) {
        }
    }
}