package com.example.memorymuseum.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path rootLocation;

    public FileStorageServiceImpl(@Value("${file.upload-dir:./uploads}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir);
    }

    @Override
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    @Override
    public String store(MultipartFile file, String subfolder) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Failed to store empty file.");
        }
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = "";
        int i = originalFilename.lastIndexOf('.');
        if (i > 0) {
            extension = originalFilename.substring(i);
        }
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        try {
            Path destinationFolder = this.rootLocation.resolve(Paths.get(subfolder)).normalize().toAbsolutePath();
            if (!destinationFolder.getParent().equals(this.rootLocation.resolve(Paths.get(subfolder).getParent()).normalize().toAbsolutePath())) {
                //This is a security check
                throw new IllegalArgumentException(
                        "Cannot store file outside current directory or specified subfolder.");
            }
            if (!Files.exists(destinationFolder)) {
                Files.createDirectories(destinationFolder);
            }

            Path destinationFile = destinationFolder.resolve(uniqueFilename).normalize().toAbsolutePath();


            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return Paths.get(subfolder).resolve(uniqueFilename).toString().replace("\\", "/");
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + originalFilename, e);
        }
    }

    @Override
    public Resource loadAsResource(String filename, String subfolder) {
        try {
            Path file = rootLocation.resolve(subfolder).resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void delete(String filename, String subfolder) {
        if (filename == null || filename.isBlank()) return;
        try {
            Path file = rootLocation.resolve(subfolder).resolve(Paths.get(filename).getFileName()).normalize(); // Ensure we only get filename
            FileSystemUtils.deleteRecursively(file);
        } catch (IOException e) {
            // Log this or handle more gracefully
            System.err.println("Failed to delete file: " + filename + " in " + subfolder + " Error: " + e.getMessage());
        }
    }

    @Override
    public Path getRootLocation() {
        return this.rootLocation;
    }
}