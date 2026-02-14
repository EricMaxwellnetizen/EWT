package com.htc.enter.service;

import java.util.List;
import com.htc.enter.model.SlaRule;
import com.htc.enter.dto.SlaRuleDTO;

public interface SlaRuleService {
    SlaRule save(SlaRule sla);
    SlaRule findById(Long id);
    List<SlaRule> findAll();
    void deleteById(Long id);

    SlaRule createFromDTO(SlaRuleDTO dto);
    SlaRule updateFromDTO(Long id, SlaRuleDTO dto);
}