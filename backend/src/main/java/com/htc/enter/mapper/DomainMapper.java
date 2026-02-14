package com.htc.enter.mapper;

import org.springframework.stereotype.Component;

import com.htc.enter.dto.ClientDTO;
import com.htc.enter.dto.ProjectDTO;
import com.htc.enter.dto.StoryDTO;
import com.htc.enter.dto.EpicDTO;
import com.htc.enter.dto.SlaRuleDTO;
import com.htc.enter.model.Client;
import com.htc.enter.model.Project;
import com.htc.enter.model.Story;
import com.htc.enter.model.SlaRule;
import com.htc.enter.model.Epic;
import com.htc.enter.model.User;

import java.time.LocalDate;

@Component
public class DomainMapper {

    // Client
    public ClientDTO toClientDTO(Client client) {
        if (client == null) return null;
        ClientDTO dto = new ClientDTO();
        dto.setClientId(client.getClient_id());
        dto.setName(client.getName());
        dto.setEmail(client.getEmail());
        dto.setPhoneNumber(client.getPhn_no());
        dto.setAddress(client.getAddress());
        return dto;
    }

    public Client toClient(ClientDTO dto) {
        if (dto == null) return null;
        Client c = new Client();
        if (dto.getClientId() != null) c.setClient_id(dto.getClientId());
        c.setName(dto.getName());
        c.setEmail(dto.getEmail());
        c.setPhn_no(dto.getPhoneNumber());
        c.setAddress(dto.getAddress());
        return c;
    }

    // Project
    public ProjectDTO toProjectDTO(Project project) {
        if (project == null) return null;
        ProjectDTO dto = new ProjectDTO();
        dto.setProjectId(project.getProjectId());
        dto.setName(project.getName());
        if (project.getClient_id() != null) dto.setClientId(project.getClient_id().getClient_id());
        if (project.getCreated_by() != null) dto.setCreatedById(project.getCreated_by().getId());
        if (project.getManager_id() != null) dto.setManagerId(project.getManager_id().getId());
        dto.setDeliverables(project.getDeliverables());
        dto.setDeadline(project.getDeadline());
        dto.setIsApproved(project.isIs_approved());
        dto.setIsEnd(project.getIs_end());
        return dto;
    }

    public Project toProject(ProjectDTO dto) {
        if (dto == null) return null;
        Project p = new Project();
        if (dto.getProjectId() != null) p.setProjectId(dto.getProjectId());
        p.setName(dto.getName());
        p.setDeliverables(dto.getDeliverables());
        p.setDeadline(dto.getDeadline());
        if (dto.getIsApproved() != null) p.setIs_approved(dto.getIsApproved());
        p.setIs_end(dto.getIsEnd());

        // map relationships using IDs
        if (dto.getClientId() != null) {
            Client client = new Client();
            client.setClient_id(dto.getClientId());
            p.setClient_id(client);
        }
        if (dto.getCreatedById() != null) {
            User creator = new User();
            creator.setId(dto.getCreatedById());
            p.setCreated_by(creator);
        }
        if (dto.getManagerId() != null) {
            User manager = new User();
            manager.setId(dto.getManagerId());
            p.setManager_id(manager);
        }

        return p;
    }


    // WorkflowStates (Epic)
    public EpicDTO toWorkflowStateDTO(Epic ws) {
        if (ws == null) return null;
        EpicDTO dto = new EpicDTO();
        dto.setWorkflowStateId(ws.getEpicId());
        if (ws.getProjectId() != null) dto.setProjectId(ws.getProjectId().getProjectId());
        if (ws.getManager_id() != null) dto.setManagerId(ws.getManager_id().getId());
        if (ws.getCreated_by() != null) dto.setCreatedById(ws.getCreated_by().getId());
        dto.setName(ws.getName());
        dto.setIsStart(ws.getIs_start());
        dto.setIsEnd(ws.getIs_end());
        dto.setDeadline(ws.getDeadline());
        dto.setDeliverables(ws.getDeliverables());
        dto.setIsApproved(ws.isIs_approved());
        return dto;
    }

    public Epic toWorkflowState(EpicDTO dto) {
        if (dto == null) return null;
        Epic ws = new Epic();
        if (dto.getWorkflowStateId() != null) ws.setEpicId(dto.getWorkflowStateId());
        ws.setName(dto.getName());
        ws.setIs_end(dto.getIsEnd());
        ws.setDeadline(dto.getDeadline());
        ws.setDeliverables(dto.getDeliverables());
        if (dto.getIsApproved() != null) ws.setIs_approved(dto.getIsApproved());

        // map project relation if provided
        if (dto.getProjectId() != null) {
            Project project = new Project();
            project.setProjectId(dto.getProjectId());
            ws.setProjectId(project);
        }

        // map manager relation if provided
        if (dto.getManagerId() != null) {
            User manager = new User();
            manager.setId(dto.getManagerId());
            ws.setManager_id(manager);
        }

        if (dto.getCreatedById() != null) {
            User creator = new User();
            creator.setId(dto.getCreatedById());
            ws.setCreated_by(creator);
        }

        return ws;
    }

    // Task (Story)
    public StoryDTO toTaskDTO(Story story) {
        if (story == null) return null;
        StoryDTO dto = new StoryDTO();
        dto.setStoryId(story.getStoryId());
        dto.setTitle(story.getTitle());
        dto.setDescription(story.getDeliverables());
        dto.setDeliverables(story.getDeliverables());
        dto.setDueDate(story.getDueDate());
        if (story.getProjectId() != null) dto.setProjectId(story.getProjectId().getProjectId());
        if (story.getAssigned_to() != null) dto.setAssignedToId(story.getAssigned_to().getId());
        if (story.getCreated_by() != null) dto.setCreatedById(story.getCreated_by().getId());
        if (story.getEpicId() != null) dto.setWorkflowStateId(story.getEpicId().getEpicId());
        dto.setDeadline(story.getDeadline());
        dto.setIsApproved(story.isIs_approved());
        dto.setIsEnd(story.getIs_end());
        return dto;
    }

    public Story toTask(StoryDTO dto) {
        if (dto == null) return null;
        Story t = new Story();
        if (dto.getStoryId() != null) t.setStoryId(dto.getStoryId());
        t.setTitle(dto.getTitle());
        t.setDeliverables(dto.getDescription() != null ? dto.getDescription() : dto.getDeliverables());
        t.setDueDate(dto.getDueDate());
        t.setDeadline(dto.getDeadline());
        if (dto.getIsApproved() != null) t.setIs_approved(dto.getIsApproved());
        t.setIs_end(dto.getIsEnd());

        // map relationships using IDs
        if (dto.getProjectId() != null) {
            Project project = new Project();
            project.setProjectId(dto.getProjectId());
            t.setProjectId(project);
        }
        if (dto.getAssignedToId() != null) {
            User user = new User();
            user.setId(dto.getAssignedToId());
            t.setAssigned_to(user);
        }
        if (dto.getCreatedById() != null) {
            User creator = new User();
            creator.setId(dto.getCreatedById());
            t.setCreated_by(creator);
        }
        if (dto.getWorkflowStateId() != null) {
            Epic epic = new Epic();
            epic.setEpicId(dto.getWorkflowStateId());
            t.setEpicId(epic);
        }

        return t;
    }

    // SlaRule
    public SlaRuleDTO toSlaRuleDTO(SlaRule sla) {
        if (sla == null) return null;
        SlaRuleDTO dto = new SlaRuleDTO();
        dto.setSlaId(sla.getSlaId());
        if (sla.getProject() != null) dto.setProjectId(sla.getProject().getProjectId());
        if (sla.getState() != null) dto.setStateId(sla.getState().getEpicId());
        dto.setDurationHours(sla.getDurationHours());
        dto.setStartPoint(sla.getStartPoint());
        if (sla.getEscalationRole() != null) dto.setEscalationRoleId(sla.getEscalationRole().getProjectId());
        dto.setEscalationDelayHours(sla.getEscalationDelayHours());
        dto.setPriority(sla.getPriority());
        dto.setNotifyEmail(sla.isNotifyEmail());
        return dto;
    }

    public SlaRule toSlaRule(SlaRuleDTO dto) {
        if (dto == null) return null;
        SlaRule s = new SlaRule();
        if (dto.getSlaId() != null) s.setSlaId(dto.getSlaId());
        s.setDurationHours(dto.getDurationHours());
        s.setStartPoint(dto.getStartPoint());
        s.setEscalationDelayHours(dto.getEscalationDelayHours());
        s.setPriority(dto.getPriority());
        s.setNotifyEmail(dto.isNotifyEmail());

        // map relations using IDs
        if (dto.getProjectId() != null) {
            Project project = new Project();
            project.setProjectId(dto.getProjectId());
            s.setProject(project);
        }
        if (dto.getStateId() != null) {
            Epic epic = new Epic();
            epic.setEpicId(dto.getStateId());
            s.setState(epic);
        }

        return s;
    }
}
