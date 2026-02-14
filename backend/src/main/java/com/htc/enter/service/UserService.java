package com.htc.enter.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.htc.enter.model.Project;
import com.htc.enter.model.User;
import com.htc.enter.dto.UserInput;

public interface UserService {
	User save(User user);
	Optional<User> findById(Long id);
    List<User> findAll();
    Page<User> findAll(Pageable pageable);
    void deleteById(Long id);

    // create a new User entity from a UserInput DTO and persist
    // Validates that creator has higher access level than the user being created
    User createFromInput(UserInput input, User creator);

    // update existing user using UserInput (find by id + save)
    User updateFromInput(Long id, UserInput input);
    
    // change user's password with verification
    void changePassword(Long userId, String oldPassword, String newPassword);
}