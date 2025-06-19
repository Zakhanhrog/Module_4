package com.example.memorymuseum.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface FileStorageService {
    void init();
    String store(MultipartFile file, String subfolder);
    Resource loadAsResource(String filename, String subfolder);
    void delete(String filename, String subfolder);
    Path getRootLocation();
}