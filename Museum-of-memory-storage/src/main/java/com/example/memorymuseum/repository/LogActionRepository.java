package com.example.memorymuseum.repository;

import com.example.memorymuseum.model.LogAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface LogActionRepository extends JpaRepository<LogAction, Long>, JpaSpecificationExecutor<LogAction> {
    Page<LogAction> findByUsername(String username, Pageable pageable);
    Page<LogAction> findByActionType(String actionType, Pageable pageable);
    Page<LogAction> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}