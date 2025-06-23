package com.artflowstudio.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Tên khóa học không được để trống")
    @Column(nullable = false, length = 255)
    private String name;

    @Lob // Dùng cho các trường văn bản dài
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500) // Đường dẫn tới hình ảnh
    private String imageUrl;

    @NotNull(message = "Học phí không được để trống")
    @Min(value = 0, message = "Học phí phải là số dương")
    private Double tuitionFee;

    @Column(length = 255)
    private String targetAudience; // Đối tượng học viên

    @Column(length = 100)
    private String duration; // Thời lượng khóa học

     @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
     private Set<ClassSchedule> classSchedules = new HashSet<>();
}