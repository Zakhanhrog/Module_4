package com.example.memorymuseum.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "log_actions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50) // Username
    private String username;

    @Column(nullable = false, length = 100)
    private String actionType; // CREATE_MEMORY, LOGIN_SUCCESS, VIEW_TIMELINE

    @Column(length = 100)
    private String targetId; // ID của memory, user, comment...

    @Lob
    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(length = 45)
    private String ipAddress;

    @CreationTimestamp
    private LocalDateTime timestamp;
}