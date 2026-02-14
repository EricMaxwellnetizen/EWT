package com.htc.enter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.htc.enter.model.Admin;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

}
