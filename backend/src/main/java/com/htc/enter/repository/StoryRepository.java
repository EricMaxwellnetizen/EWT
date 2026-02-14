package com.htc.enter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.htc.enter.model.Story;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {
    
    @Query("SELECT s FROM Story s WHERE s.EpicId.epicId = :epicId")
    List<Story> findByEpicId(@Param("epicId") Long epicId);

}