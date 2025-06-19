package com.example.memorymuseum.repository;

import com.example.memorymuseum.model.LogAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogActionRepository extends JpaRepository<LogAction, Long> {
}