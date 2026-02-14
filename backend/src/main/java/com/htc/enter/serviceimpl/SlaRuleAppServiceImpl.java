package com.htc.enter.serviceimpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.htc.enter.dto.DocumentPayload;
import com.htc.enter.model.SlaRule;
import com.htc.enter.model.User;
import com.htc.enter.service.SlaRuleAppService;
import com.htc.enter.service.SlaRuleDocumentService;
import com.htc.enter.service.SlaRuleService;
import com.htc.enter.service.UserAuthService;
import com.htc.enter.util.DocumentPasswordUtil;

@Service
public class SlaRuleAppServiceImpl implements SlaRuleAppService {

    private final SlaRuleService slaRuleService;
    private final SlaRuleDocumentService documentService;
    private final UserAuthService authService;

    public SlaRuleAppServiceImpl(SlaRuleService slaRuleService,
                                 SlaRuleDocumentService documentService,
                                 UserAuthService authService) {
        this.slaRuleService = slaRuleService;
        this.documentService = documentService;
        this.authService = authService;
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentPayload buildSlaRulesDocument() throws IOException {
        List<SlaRule> slaRules = slaRuleService.findAll();
        User current = authService.getCurrentUser();
        String password = DocumentPasswordUtil.resolvePassword(current, null);

        ByteArrayOutputStream os = documentService.generatePasswordProtectedSlaRulesDocument(slaRules, password);
        String fileName = "sla_rules_report.docx";
        return DocumentPayload.ofDocx(os.toByteArray(), fileName);
    }
}