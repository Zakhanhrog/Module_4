package com.example.memorymuseum.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "emotion_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmotionType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty
    @Column(nullable = false, unique = true, length = 50)
    private String name; // Vui, Buồn, Tức giận...

    @Column(length = 50)
    private String nameEn; // Happy, Sad, Angry...

    @Column(length = 255)
    private String icon; // Tên class CSS hoặc URL icon
}