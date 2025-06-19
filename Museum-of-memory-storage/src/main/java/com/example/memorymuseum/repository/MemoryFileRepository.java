package com.example.memorymuseum.repository;

import com.example.memorymuseum.model.MemoryFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemoryFileRepository extends JpaRepository<MemoryFile, Long> {
    List<MemoryFile> findByMemoryId(Long memoryId);
}