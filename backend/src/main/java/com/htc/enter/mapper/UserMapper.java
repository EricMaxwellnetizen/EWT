package com.htc.enter.mapper;

import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import com.htc.enter.dto.AdminInput;
import com.htc.enter.dto.AdminOutput;
import com.htc.enter.dto.EmployeeInput;
import com.htc.enter.dto.EmployeeOutput;
import com.htc.enter.dto.ManagerInput;
import com.htc.enter.dto.ManagerOutput;
import com.htc.enter.dto.UserInput;
import com.htc.enter.dto.UserOutput;
import com.htc.enter.model.Admin;
import com.htc.enter.model.Employee;
import com.htc.enter.model.Manager;
import com.htc.enter.model.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(source = "job_title", target = "jobTitle")
    @Mapping(source = "joining_date", target = "joiningDate")
    @Mapping(source = "reportingTo.id", target = "reportingToId")
    @Mapping(source = "reportingTo.username", target = "reportingToUsername")
    @Mapping(source = "department", target = "department")
    UserOutput toUserOutput(User user);

    @Mapping(source = "jobTitle", target = "job_title")
    @Mapping(source = "joiningDate", target = "joining_date")
    @Mapping(source = "password", target = "passwordhash")
    @Mapping(source = "department", target = "department")
    @Mapping(source = "reportingToId", target = "reportingTo", qualifiedByName = "mapReportingTo")
    User toUser(UserInput input);

    @Mapping(source = "job_title", target = "jobTitle")
    @Mapping(source = "joining_date", target = "joiningDate")
    @Mapping(source = "reportingTo.id", target = "reportingToId")
    @Mapping(source = "reportingTo.username", target = "reportingToUsername")
    @Mapping(source = "admin_level", target = "adminLevel")
    @Mapping(source = "accessScope", target = "accessScope")
    @Mapping(source = "department", target = "department")
    AdminOutput toAdminOutput(Admin admin);

    @Mapping(source = "jobTitle", target = "job_title")
    @Mapping(source = "joiningDate", target = "joining_date")
    @Mapping(source = "adminLevel", target = "admin_level")
    @Mapping(source = "accessScope", target = "accessScope")
    @Mapping(source = "password", target = "passwordhash")
    @Mapping(source = "department", target = "department")
    @Mapping(source = "reportingToId", target = "reportingTo", qualifiedByName = "mapReportingTo")
    Admin toAdmin(AdminInput input);

    @Mapping(source = "job_title", target = "jobTitle")
    @Mapping(source = "joining_date", target = "joiningDate")
    @Mapping(source = "reportingTo.id", target = "reportingToId")
    @Mapping(source = "reportingTo.username", target = "reportingToUsername")
    @Mapping(source = "max_active_tasks", target = "maxActiveTasks")
    @Mapping(target = "skillSet", source = "skillSet", qualifiedByName = "mapSkill")
    @Mapping(source = "department", target = "department")
    EmployeeOutput toEmployeeOutput(Employee employee);

    @Mapping(source = "jobTitle", target = "job_title")
    @Mapping(source = "maxActiveTasks", target = "max_active_tasks")
    @Mapping(target = "skillSet", source = "skillSet", qualifiedByName = "mapSetToSkillSet")
    @Mapping(source = "password", target = "passwordhash")
    @Mapping(source = "department", target = "department")
    @Mapping(source = "reportingToId", target = "reportingTo", qualifiedByName = "mapReportingTo")
    Employee toEmployee(EmployeeInput input);

    @Named("mapSkill")
    default Employee.skill_set mapSkill(Employee.skill_set skill) {
        return skill;
    }

    @Named("mapSetToSkillSet")
    default Employee.skill_set mapSetToSkillSet(Set<String> skills) {
        if (skills == null || skills.isEmpty()) return null;
        return Employee.skill_set.valueOf(skills.iterator().next());
    }

    @Named("mapSkillSetToSet")
    default Set<String> mapSkillSetToSet(Employee.skill_set skill) {
        if (skill == null) return null;
        return Set.of(skill.name());
    }

    @Mapping(source = "job_title", target = "jobTitle")
    @Mapping(source = "joining_date", target = "joiningDate")
    @Mapping(source = "reportingTo.id", target = "reportingToId")
    @Mapping(source = "reportingTo.username", target = "reportingToUsername")
    @Mapping(source = "approvalLimit", target = "approvalLimit")
    @Mapping(source = "managedWorkflowCount", target = "managedWorkflowCount")
    @Mapping(source = "teamSize", target = "teamSize")
    @Mapping(source = "department", target = "department")
    ManagerOutput toManagerOutput(Manager manager);

    @Mapping(source = "jobTitle", target = "job_title")
    @Mapping(source = "approvalLimit", target = "approvalLimit")
    @Mapping(source = "managedWorkflowCount", target = "managedWorkflowCount")
    @Mapping(source = "teamSize", target = "teamSize")
    @Mapping(source = "password", target = "passwordhash")
    @Mapping(source = "department", target = "department")
    @Mapping(source = "reportingToId", target = "reportingTo", qualifiedByName = "mapReportingTo")
    Manager toManager(ManagerInput input);

    // Helper that chooses the correct target type based on runtime input
    default User toSpecificUser(UserInput input) {
        if (input == null) return null;
        if (input instanceof AdminInput ai) return toAdmin(ai);
        if (input instanceof EmployeeInput ei) return toEmployee(ei);
        if (input instanceof ManagerInput mi) return toManager(mi);
        return toUser(input);
    }

    @Named("mapReportingTo")
    default User mapReportingTo(Long reportingToId) {
        if (reportingToId == null) return null;
        User manager = new User();
        manager.setId(reportingToId);   // only set the ID, JPA will treat it as a reference
        return manager;
    }
}
