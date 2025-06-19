package com.example.memorymuseum.repository;

import com.example.memorymuseum.model.Memory;
import com.example.memorymuseum.model.MemoryStatus;
import com.example.memorymuseum.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemoryRepository extends JpaRepository<Memory, Long>, JpaSpecificationExecutor<Memory> {
    Page<Memory> findByUserOrderByMemoryDateDescCreatedAtDesc(User user, Pageable pageable);
    Page<Memory> findByStatusOrderByMemoryDateDescCreatedAtDesc(MemoryStatus status, Pageable pageable);

    @Query("SELECT m FROM Memory m WHERE m.user = :user AND (m.status = 'PUBLIC' OR m.status = 'PRIVATE') ORDER BY m.memoryDate DESC, m.createdAt DESC")
    Page<Memory> findOwnAndPublicMemoriesForUser(@Param("user") User user, Pageable pageable);

    List<Memory> findByUserAndStatus(User user, MemoryStatus status);
}