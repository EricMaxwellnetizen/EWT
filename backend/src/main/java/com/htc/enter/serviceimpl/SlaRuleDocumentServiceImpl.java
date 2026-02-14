package com.htc.enter.serviceimpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.htc.enter.model.SlaRule;
import com.htc.enter.service.SlaRuleDocumentService;

@Service
public class SlaRuleDocumentServiceImpl extends BaseDocumentService implements SlaRuleDocumentService {

    @Override
    @Transactional(readOnly = true)
    public ByteArrayOutputStream generatePasswordProtectedSlaRulesDocument(
            List<SlaRule> slaRules, String password) throws IOException {
        return generatePasswordProtectedDocument(
                "SLA Rules Report",
                "No SLA rules found.",
                slaRules,
                password,
                this::addSlaRuleContent);
    }

    private void addSlaRuleContent(XWPFDocument doc, SlaRule slaRule) {
        XWPFParagraph p = createSectionHeader(doc, "SLA Rule ID: " + slaRule.getSlaId());
        
        XWPFRun r2 = p.createRun();
        addField(r2, "Project", slaRule.getProject() != null ? 
            slaRule.getProject().getName() + " (ID: " + slaRule.getProject().getProjectId() + ")" : null);
        addField(r2, "State", slaRule.getState() != null ? 
            slaRule.getState().getName() + " (ID: " + slaRule.getState().getEpicId() + ")" : null);
        addField(r2, "Duration (hours)", slaRule.getDurationHours());
        addField(r2, "Start Point", slaRule.getStartPoint());
        addField(r2, "Escalation Delay (hours)", slaRule.getEscalationDelayHours());
        addField(r2, "Priority", slaRule.getPriority());
        addField(r2, "Email Notification", slaRule.isNotifyEmail());
        addSectionSpacing(r2);
    }
}