package com.htc.enter.service;

import java.util.List;

import com.htc.enter.model.Project;
import com.htc.enter.model.Story;
import com.htc.enter.dto.StoryDTO;

public interface StoryService {
	Story save(Story story);
	Story findById(Long id);
	    List<Story> findAll();
	    void deleteById(Long id);

    Story createFromDTO(StoryDTO dto);
    Story updateFromDTO(Long id, StoryDTO dto);

    /**
     * Mark a story as completed. Sets is_approved=true and is_end=today.
     * Cascades: if all stories in the parent epic are complete, the epic completes.
     * If all epics in the parent project are complete, the project completes.
     */
    Story completeStory(Long id);
}