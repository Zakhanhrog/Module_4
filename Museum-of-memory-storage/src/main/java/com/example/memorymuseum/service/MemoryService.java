package com.example.memorymuseum.service;

import com.example.memorymuseum.dto.MemoryDto;
import com.example.memorymuseum.dto.MemorySearchDto;
import com.example.memorymuseum.model.Memory;
import com.example.memorymuseum.model.MemoryStatus;
import com.example.memorymuseum.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface MemoryService {
    Memory saveMemory(MemoryDto memoryDto, List<MultipartFile> files, User currentUser);
    Memory updateMemory(Long memoryId, MemoryDto memoryDto, List<MultipartFile> newFiles, User currentUser);
    Optional<Memory> findByIdAndUser(Long id, User user);
    Optional<Memory> findById(Long id);
    Page<Memory> findMemoriesByUser(User user, Pageable pageable);
    Page<Memory> findPublicMemories(Pageable pageable);
    void deleteMemory(Long memoryId, User currentUser);
    
    // Các phương thức mới cho chức năng Admin
    Page<Memory> findAllMemories(Pageable pageable);
    Memory updateMemoryByAdmin(Long memoryId, MemoryDto memoryDto);
    void deleteMemoryByAdmin(Long memoryId);
    Page<Memory> searchMemories(MemorySearchDto searchDto, Pageable pageable);
}