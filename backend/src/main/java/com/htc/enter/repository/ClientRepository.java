package com.htc.enter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.htc.enter.model.Client;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long>{

	
	
	
}
