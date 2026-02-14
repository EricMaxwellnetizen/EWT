package com.htc.enter.serviceimpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.htc.enter.model.Story;
import com.htc.enter.service.StoryDocumentService;

@Service
public class StoryDocumentServiceImpl extends BaseDocumentService implements StoryDocumentService {

    @Override
    @Transactional(readOnly = true)
    public ByteArrayOutputStream generatePasswordProtectedManagerStoriesDocument(
            Long managerId, List<Story> stories, String password) throws IOException {
        return generatePasswordProtectedDocument(
                "Stories for Manager ID: " + managerId,
                "No stories found for this manager.",
                stories,
                password,
                this::addStoryContent);
    }

    private void addStoryContent(XWPFDocument doc, Story story) {
        XWPFParagraph p = createSectionHeader(doc, "Story: " + story.getTitle() + " (ID: " + story.getStoryId() + ")");
        
        XWPFRun r2 = p.createRun();
        addField(r2, "Project", story.getProjectId() != null ? 
            story.getProjectId().getName() + " (ID: " + story.getProjectId().getProjectId() + ")" : null);
        addField(r2, "Epic", story.getEpicId() != null ? 
            story.getEpicId().getName() + " (ID: " + story.getEpicId().getEpicId() + ")" : null);
        addField(r2, "Assigned To", story.getAssigned_to() != null ? 
            story.getAssigned_to().getUsername() + " (ID: " + story.getAssigned_to().getId() + ")" : null);
        addField(r2, "Manager", story.getManager() != null ? 
            story.getManager().getUsername() + " (ID: " + story.getManager().getId() + ")" : null);
        addField(r2, "Due Date", story.getDueDate());
        addField(r2, "Deadline", story.getDeadline());
        addField(r2, "End Date", story.getIs_end());
        addField(r2, "Approved", story.isIs_approved());
        addField(r2, "Deliverables", story.getDeliverables());
        addSectionSpacing(r2);
    }
}