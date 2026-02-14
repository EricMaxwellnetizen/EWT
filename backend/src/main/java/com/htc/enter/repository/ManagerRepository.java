package com.htc.enter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.htc.enter.model.Manager;

@Repository
public interface ManagerRepository extends JpaRepository<Manager, Long>{

}
