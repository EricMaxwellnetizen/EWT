package com.htc.enter.serviceimpl;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.htc.enter.dto.SlaRuleDTO;
import com.htc.enter.model.SlaRule;
import com.htc.enter.model.Project;
import com.htc.enter.model.Epic;
import com.htc.enter.repository.ProjectRepository;
import com.htc.enter.repository.SlaRuleRepository;
import com.htc.enter.repository.EpicRepository;
import com.htc.enter.service.SlaRuleService;

@Service
public class SlaRuleServiceImpl implements SlaRuleService {

    private final SlaRuleRepository repo;
    private final ProjectRepository projectRepo;
    private final EpicRepository stateRepo;

    public SlaRuleServiceImpl(SlaRuleRepository repo, ProjectRepository projectRepo, EpicRepository stateRepo) {
        this.repo = repo;
        this.projectRepo = projectRepo;
        this.stateRepo = stateRepo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "slaRules", allEntries = true)
    public SlaRule save(SlaRule sla) {
        return repo.save(sla);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "slaRules", key = "#id")
    public SlaRule findById(Long id) {
        return repo.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "slaRules")
    public List<SlaRule> findAll() {
        return repo.findAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "slaRules", allEntries = true)
    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "slaRules", allEntries = true)
    public SlaRule createFromDTO(SlaRuleDTO dto) {
        SlaRule s = new SlaRule();
        s.setDurationHours(dto.getDurationHours());
        s.setStartPoint(dto.getStartPoint());
        s.setEscalationDelayHours(dto.getEscalationDelayHours());
        s.setPriority(dto.getPriority());
        s.setNotifyEmail(dto.isNotifyEmail());
        if (dto.getProjectId() != null) {
            Project p = projectRepo.findById(dto.getProjectId()).orElse(null);
            s.setProject(p);
        }
        if (dto.getStateId() != null) {
            Epic ws = stateRepo.findById(dto.getStateId()).orElse(null);
            s.setState(ws);
        }
        if (dto.getEscalationRoleId() != null) {
            Project er = projectRepo.findById(dto.getEscalationRoleId()).orElse(null);
            s.setEscalationRole(er);
        }
        return repo.save(s);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "slaRules", allEntries = true)
    public SlaRule updateFromDTO(Long id, SlaRuleDTO dto) {
        SlaRule existing = findById(id);
        if (existing == null) throw new IllegalArgumentException("SLA rule not found with id: " + id);
        if (dto.getDurationHours() != 0) existing.setDurationHours(dto.getDurationHours());
        if (dto.getStartPoint() != null) existing.setStartPoint(dto.getStartPoint());
        if (dto.getEscalationDelayHours() != 0) existing.setEscalationDelayHours(dto.getEscalationDelayHours());
        if (dto.getPriority() != null) existing.setPriority(dto.getPriority());
        existing.setNotifyEmail(dto.isNotifyEmail());
        if (dto.getProjectId() != null) {
            Project p = projectRepo.findById(dto.getProjectId()).orElse(null);
            existing.setProject(p);
        }
        if (dto.getStateId() != null) {
            Epic ws = stateRepo.findById(dto.getStateId()).orElse(null);
            existing.setState(ws);
        }
        if (dto.getEscalationRoleId() != null) {
            Project er = projectRepo.findById(dto.getEscalationRoleId()).orElse(null);
            existing.setEscalationRole(er);
        }
        return repo.save(existing);
    }
}