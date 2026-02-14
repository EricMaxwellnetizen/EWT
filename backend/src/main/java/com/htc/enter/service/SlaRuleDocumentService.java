package com.htc.enter.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import com.htc.enter.model.SlaRule;

public interface SlaRuleDocumentService {
    ByteArrayOutputStream generatePasswordProtectedSlaRulesDocument(List<SlaRule> slaRules, String password) throws IOException;
}
