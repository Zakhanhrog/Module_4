package com.example.memorymuseum.service;

import com.example.memorymuseum.dto.MemoryDto;
import com.example.memorymuseum.model.EmotionType;
import com.example.memorymuseum.model.Memory;
import com.example.memorymuseum.model.MemoryFile;
import com.example.memorymuseum.model.User;
import com.example.memorymuseum.repository.EmotionTypeRepository;
import com.example.memorymuseum.repository.MemoryFileRepository;
import com.example.memorymuseum.repository.MemoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MemoryServiceImpl implements MemoryService {

    private final MemoryRepository memoryRepository;
    private final EmotionTypeRepository emotionTypeRepository;
    private final FileStorageService fileStorageService;
    private final MemoryFileRepository memoryFileRepository;
    private static final String MEMORY_FILES_SUBFOLDER = "memories";


    public MemoryServiceImpl(MemoryRepository memoryRepository,
                             EmotionTypeRepository emotionTypeRepository,
                             FileStorageService fileStorageService,
                             MemoryFileRepository memoryFileRepository) {
        this.memoryRepository = memoryRepository;
        this.emotionTypeRepository = emotionTypeRepository;
        this.fileStorageService = fileStorageService;
        this.memoryFileRepository = memoryFileRepository;
    }

    @Override
    @Transactional
    public Memory saveMemory(MemoryDto memoryDto, List<MultipartFile> files, User currentUser) {
        Memory memory = new Memory();
        memory.setUser(currentUser);
        memory.setTitle(memoryDto.getTitle());
        memory.setDescription(memoryDto.getDescription());
        memory.setMemoryDate(memoryDto.getMemoryDate());
        memory.setLocation(memoryDto.getLocation());
        memory.setStatus(memoryDto.getStatus());

        if (memoryDto.getEmotionTypeId() != null) {
            EmotionType emotion = emotionTypeRepository.findById(memoryDto.getEmotionTypeId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid emotion type ID: " + memoryDto.getEmotionTypeId()));
            memory.setEmotionType(emotion);
        }

        Memory savedMemory = memoryRepository.save(memory);

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String storedFileName = fileStorageService.store(file, MEMORY_FILES_SUBFOLDER + "/" + savedMemory.getId());
                    MemoryFile memoryFile = new MemoryFile();
                    memoryFile.setMemory(savedMemory);
                    memoryFile.setFilePath(storedFileName);
                    memoryFile.setOriginalFileName(file.getOriginalFilename());
                    memoryFile.setFileType(file.getContentType());
                    memoryFile.setFileSize(file.getSize());
                    memoryFileRepository.save(memoryFile);
                    savedMemory.getFiles().add(memoryFile);
                }
            }
        }
        return savedMemory;
    }

    @Override
    @Transactional
    public Memory updateMemory(Long memoryId, MemoryDto memoryDto, List<MultipartFile> newFiles, User currentUser) {
        Memory memory = memoryRepository.findById(memoryId)
                .orElseThrow(() -> new IllegalArgumentException("Memory not found with ID: " + memoryId));

        if (!memory.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to update this memory.");
        }

        memory.setTitle(memoryDto.getTitle());
        memory.setDescription(memoryDto.getDescription());
        memory.setMemoryDate(memoryDto.getMemoryDate());
        memory.setLocation(memoryDto.getLocation());
        memory.setStatus(memoryDto.getStatus());

        if (memoryDto.getEmotionTypeId() != null) {
            EmotionType emotion = emotionTypeRepository.findById(memoryDto.getEmotionTypeId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid emotion type ID: " + memoryDto.getEmotionTypeId()));
            memory.setEmotionType(emotion);
        } else {
            memory.setEmotionType(null);
        }

        if (memoryDto.getFilesToDeleteIds() != null) {
            for (Long fileIdToDelete : memoryDto.getFilesToDeleteIds()) {
                MemoryFile fileToDelete = memoryFileRepository.findById(fileIdToDelete)
                        .orElseThrow(() -> new IllegalArgumentException("File not found to delete: " + fileIdToDelete));
                if (fileToDelete.getMemory().getId().equals(memoryId)) {
                    fileStorageService.delete(Paths.get(fileToDelete.getFilePath()).getFileName().toString(), MEMORY_FILES_SUBFOLDER + "/" + memoryId);
                    memoryFileRepository.delete(fileToDelete);
                    memory.getFiles().removeIf(f -> f.getId().equals(fileIdToDelete));
                }
            }
        }


        if (newFiles != null && !newFiles.isEmpty()) {
            for (MultipartFile file : newFiles) {
                if (!file.isEmpty()) {
                    String storedFileName = fileStorageService.store(file, MEMORY_FILES_SUBFOLDER + "/" + memory.getId());
                    MemoryFile memoryFile = new MemoryFile();
                    memoryFile.setMemory(memory);
                    memoryFile.setFilePath(storedFileName);
                    memoryFile.setOriginalFileName(file.getOriginalFilename());
                    memoryFile.setFileType(file.getContentType());
                    memoryFile.setFileSize(file.getSize());
                    memoryFileRepository.save(memoryFile);
                    memory.getFiles().add(memoryFile);
                }
            }
        }
        return memoryRepository.save(memory);
    }

    @Override
    public Optional<Memory> findByIdAndUser(Long id, User user) {
        Optional<Memory> memoryOpt = memoryRepository.findById(id);
        if (memoryOpt.isPresent() && memoryOpt.get().getUser().getId().equals(user.getId())) {
            return memoryOpt;
        }
        return Optional.empty();
    }

    @Override
    public Optional<Memory> findById(Long id) {
        return memoryRepository.findById(id);
    }

    @Override
    public Page<Memory> findMemoriesByUser(User user, Pageable pageable) {
        return memoryRepository.findByUserOrderByMemoryDateDescCreatedAtDesc(user, pageable);
    }

    @Override
    public Page<Memory> findPublicMemories(Pageable pageable) {
        return memoryRepository.findByStatusOrderByMemoryDateDescCreatedAtDesc(com.example.memorymuseum.model.MemoryStatus.PUBLIC, pageable);
    }

    @Override
    @Transactional
    public void deleteMemory(Long memoryId, User currentUser) {
        Memory memory = memoryRepository.findById(memoryId)
                .orElseThrow(() -> new IllegalArgumentException("Memory not found with ID: " + memoryId));

        if (!memory.getUser().getId().equals(currentUser.getId()) && currentUser.getRole() != com.example.memorymuseum.model.Role.ADMIN) {
            throw new AccessDeniedException("You are not authorized to delete this memory.");
        }

        List<MemoryFile> filesToDelete = new ArrayList<>(memory.getFiles());
        for (MemoryFile file : filesToDelete) {
            fileStorageService.delete(Paths.get(file.getFilePath()).getFileName().toString(), MEMORY_FILES_SUBFOLDER + "/" + memoryId);
        }
        // Cascade delete should handle memoryFileRepository.deleteAll(filesToDelete) but can do it explicitly if needed.
        memoryRepository.delete(memory);
        // Also delete the memory subfolder if it's empty
        // fileStorageService.deleteSubfolderIfEmpty(MEMORY_FILES_SUBFOLDER + "/" + memoryId); // Cần implement
    }
}