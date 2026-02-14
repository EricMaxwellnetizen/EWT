package com.htc.enter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.htc.enter.model.Epic;

@Repository
public interface EpicRepository extends JpaRepository<Epic, Long>{

    Optional<Epic> findByName(String name);
    
    @Query("SELECT e FROM Epic e WHERE e.projectId.projectId = :projectId")
    List<Epic> findByProjectId(@Param("projectId") Long projectId);
    
}