package com.htc.enter.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.htc.enter.model.TokenRecord;

@Repository
public interface TokenRepository extends JpaRepository<TokenRecord, Long> {
    Optional<TokenRecord> findByToken(String token);
}
