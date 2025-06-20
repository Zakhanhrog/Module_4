package com.example.memorymuseum.service;

import com.example.memorymuseum.model.LogAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface LogActionService {
    LogAction save(LogAction logAction);
    LogAction createLogEntry(String username, String actionType, String targetId, String details, String ipAddress);
    Page<LogAction> findAll(Pageable pageable);
    Page<LogAction> findByUsername(String username, Pageable pageable);
    Page<LogAction> findByActionType(String actionType, Pageable pageable);
    Page<LogAction> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    Page<LogAction> findByFilters(String username, String actionType, 
                          LocalDateTime startDate, LocalDateTime endDate, 
                          Pageable pageable);
}
