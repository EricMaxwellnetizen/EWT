package com.htc.enter.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import com.htc.enter.model.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>{

    @Query("SELECT p FROM Project p WHERE p.manager_id.id = :managerId")
    List<Project> findByManagerId(@Param("managerId") Long managerId);
    
    @Query("SELECT p FROM Project p WHERE p.manager_id.id = :managerId")
    Page<Project> findByManagerId(@Param("managerId") Long managerId, Pageable pageable);
    
    @Query("SELECT p FROM Project p WHERE p.client_id.client_id = :clientId")
    List<Project> findByClientId(@Param("clientId") Long clientId);

}