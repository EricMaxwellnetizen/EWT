package com.htc.enter.serviceimpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.htc.enter.model.Epic;
import com.htc.enter.service.EpicDocumentService;

@Service
public class EpicDocumentServiceImpl extends BaseDocumentService implements EpicDocumentService {

    @Override
    @Transactional(readOnly = true)
    public ByteArrayOutputStream generatePasswordProtectedManagerEpicsDocument(
            Long managerId, List<Epic> epics, String password) throws IOException {
        return generatePasswordProtectedDocument(
                "Epics for Manager ID: " + managerId,
                "No epics found for this manager.",
                epics,
                password,
                this::addEpicContent);
    }

    private void addEpicContent(XWPFDocument doc, Epic epic) {
        XWPFParagraph p = createSectionHeader(doc, "Epic: " + epic.getName() + " (ID: " + epic.getEpicId() + ")");
        
        XWPFRun r2 = p.createRun();
        addField(r2, "Project", epic.getProjectId() != null ? 
            epic.getProjectId().getName() + " (ID: " + epic.getProjectId().getProjectId() + ")" : null);
        addField(r2, "Manager", epic.getManager_id() != null ? 
            epic.getManager_id().getUsername() + " (ID: " + epic.getManager_id().getId() + ")" : null);
        addField(r2, "Start Date", epic.getIs_start());
        addField(r2, "End Date", epic.getIs_end());
        addField(r2, "Deadline", epic.getDeadline());
        addField(r2, "Approved", epic.isIs_approved());
        addField(r2, "Deliverables", epic.getDeliverables());
        addSectionSpacing(r2);
    }
}