package com.example.memorymuseum.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "memory_files")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "memory")
@EqualsAndHashCode(exclude = "memory")
public class MemoryFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memory_id", nullable = false)
    private Memory memory;

    @Column(nullable = false, length = 500)
    private String filePath;

    @Column(nullable = false, length = 255)
    private String originalFileName;

    @Column(length = 100)
    private String fileType;

    private Long fileSize;

    @CreationTimestamp
    private LocalDateTime uploadedAt;
}