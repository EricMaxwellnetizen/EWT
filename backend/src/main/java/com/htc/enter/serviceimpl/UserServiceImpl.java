package com.htc.enter.serviceimpl;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.htc.enter.dto.AdminInput;
import com.htc.enter.dto.EmployeeInput;
import com.htc.enter.dto.ManagerInput;
import com.htc.enter.dto.UserInput;
import com.htc.enter.exception.ForbiddenException;
import com.htc.enter.mapper.UserMapper;
import com.htc.enter.model.Admin;
import com.htc.enter.model.Employee;
import com.htc.enter.model.Manager;
import com.htc.enter.model.User;
import com.htc.enter.repository.UserRepository;
import com.htc.enter.service.UserService;
import com.htc.enter.util.AccessControlUtil;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repo;
    private final UserMapper mapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AccessControlUtil accessControlUtil;

    public UserServiceImpl(UserRepository repo, UserMapper mapper, BCryptPasswordEncoder bCryptPasswordEncoder,
                         AccessControlUtil accessControlUtil) {
        this.repo = repo;
        this.mapper = mapper;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.accessControlUtil = accessControlUtil;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "users", allEntries = true)
    public User save(User user) {
        if (user.getPasswordhash() != null && !user.getPasswordhash().isEmpty()) {
            user.setPasswordhash(bCryptPasswordEncoder.encode(user.getPasswordhash()));
        }

        if (user instanceof Admin a) System.out.println("Saving Admin level: " + a.getAdmin_level());
        else if (user instanceof Employee e) System.out.println("Saving Employee max tasks: " + e.getMax_active_tasks());
        else if (user instanceof Manager m) System.out.println("Saving Manager team size: " + m.getTeamSize());

        return repo.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#id")
    public Optional<User> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users")
    public List<User> findAll() {
        return repo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> findAll(Pageable pageable) {
        return repo.findAll(pageable);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "users", allEntries = true)
    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public User updateUser(User existing, UserInput input) {
        if (input.getUsername() != null) existing.setUsername(input.getUsername());
        if (input.getEmail() != null) existing.setEmail(input.getEmail());
        if (input.getPassword() != null) existing.setPasswordhash(bCryptPasswordEncoder.encode(input.getPassword()));
        if (input.getJobTitle() != null) existing.setJob_title(input.getJobTitle());
        if (input.getJoiningDate() != null) existing.setJoining_date(input.getJoiningDate());
        if (input.getDepartment() != null) existing.setDepartment(input.getDepartment());

        if (existing instanceof Admin a && input instanceof AdminInput ai) {
            a.setAdmin_level(ai.getAdminLevel());
            if (ai.getAccessScope() != null) {
                a.setAccessScope(Admin.access_Scope.valueOf(ai.getAccessScope()));
            }
        } else if (existing instanceof Employee e && input instanceof EmployeeInput ei) {
            if (ei.getMaxActiveTasks() != null) e.setMax_active_tasks(ei.getMaxActiveTasks());
            if (ei.getSkillSet() != null && !ei.getSkillSet().isEmpty()) {
                String firstSkill = ei.getSkillSet().iterator().next();
                e.setSkillSet(Employee.skill_set.valueOf(firstSkill));
            }
        } else if (existing instanceof Manager m && input instanceof ManagerInput mi) {
            if (mi.getApprovalLimit() != null) m.setApprovalLimit(mi.getApprovalLimit());
            if (mi.getManagedWorkflowCount() != null) m.setManagedWorkflowCount(mi.getManagedWorkflowCount());
            if (mi.getTeamSize() != null) m.setTeamSize(mi.getTeamSize());
        }

        return repo.save(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "users", allEntries = true)
    public User createFromInput(UserInput input, User creator) {
        // Validate access level: creator must have higher access level than the user being created
        Integer creatorAccessLevel = creator.getAccessLevel();
        Integer newUserAccessLevel = input.getAccessLevel();
        
        // Default to level 1 if not specified
        if (newUserAccessLevel == null) {
            newUserAccessLevel = 1;
        }
        
        if (creatorAccessLevel == null) {
            throw new IllegalArgumentException("Creator user has no access level assigned");
        }
        
        // Enforce: creator's access level must be STRICTLY GREATER than new user's access level
        if (creatorAccessLevel <= newUserAccessLevel) {
            throw new ForbiddenException(
                String.format(
                    "Access denied: You (access level %d) can only create users with access level less than %d. " +
                    "Requested user access level: %d",
                    creatorAccessLevel,
                    creatorAccessLevel,
                    newUserAccessLevel
                )
            );
        }
        
        User user = mapper.toSpecificUser(input);

        if (input.getPassword() != null) {
            user.setPasswordhash(bCryptPasswordEncoder.encode(input.getPassword()));
        } else {
            throw new IllegalArgumentException("Password missing on created user entity");
        }

        if (input.getReportingToId() != null) {
            User manager = repo.findById(input.getReportingToId())
                    .orElseThrow(() -> new IllegalArgumentException("Reporting user not found with id: " + input.getReportingToId()));
            user.setReportingTo(manager);
        }

        if (input.getAccessLevel() != null) {
            user.setAccessLevel(input.getAccessLevel());
        }

        return repo.save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "users", allEntries = true)
    public User updateFromInput(Long id, UserInput input) {
        User existing = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        // Check access control: user must have permission to edit
        User currentUser = accessControlUtil.getCurrentUser();
        accessControlUtil.checkEditPermission(currentUser, existing);

        if (input.getReportingToId() != null) {
            User manager = repo.findById(input.getReportingToId())
                    .orElseThrow(() -> new IllegalArgumentException("Reporting user not found with id: " + input.getReportingToId()));
            existing.setReportingTo(manager);
        }

        if (input.getAccessLevel() != null) {
            existing.setAccessLevel(input.getAccessLevel());
        }

        return updateUser(existing, input);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = repo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        // Verify old password matches
        if (!bCryptPasswordEncoder.matches(oldPassword, user.getPasswordhash())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }
        
        // Set new password
        user.setPasswordhash(bCryptPasswordEncoder.encode(newPassword));
        repo.save(user);
    }

}