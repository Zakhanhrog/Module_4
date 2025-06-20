package com.example.memorymuseum.service.impl;

import com.example.memorymuseum.model.LogAction;
import com.example.memorymuseum.repository.LogActionRepository;
import com.example.memorymuseum.service.LogActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class LogActionServiceImpl implements LogActionService {

    private final LogActionRepository logActionRepository;

    @Autowired
    public LogActionServiceImpl(LogActionRepository logActionRepository) {
        this.logActionRepository = logActionRepository;
    }

    @Override
    @Transactional
    public LogAction save(LogAction logAction) {
        return logActionRepository.save(logAction);
    }

    @Override
    @Transactional
    public LogAction createLogEntry(String username, String actionType, String targetId, String details, String ipAddress) {
        LogAction logAction = new LogAction();
        logAction.setUsername(username);
        logAction.setActionType(actionType);
        logAction.setTargetId(targetId);
        logAction.setDetails(details);
        logAction.setIpAddress(ipAddress);
        // Timestamp được tự động thiết lập bởi @CreationTimestamp
        return logActionRepository.save(logAction);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LogAction> findAll(Pageable pageable) {
        return logActionRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LogAction> findByUsername(String username, Pageable pageable) {
        return logActionRepository.findByUsername(username, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LogAction> findByActionType(String actionType, Pageable pageable) {
        return logActionRepository.findByActionType(actionType, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LogAction> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return logActionRepository.findByTimestampBetween(startDate, endDate, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LogAction> findByFilters(String username, String actionType, 
                                LocalDateTime startDate, LocalDateTime endDate, 
                                Pageable pageable) {
        
        Specification<LogAction> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (username != null && !username.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("username"), username));
            }
            
            if (actionType != null && !actionType.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("actionType"), actionType));
            }
            
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), startDate));
            }
            
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"), endDate));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        return logActionRepository.findAll(specification, pageable);
    }
}
