package com.htc.enter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.htc.enter.model.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>{

}
