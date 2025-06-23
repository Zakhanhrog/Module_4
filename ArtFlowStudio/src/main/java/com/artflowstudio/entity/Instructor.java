package com.artflowstudio.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "instructors")
@Getter
@Setter
@NoArgsConstructor
public class Instructor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Tên giảng viên không được để trống")
    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String specialization; // Chuyên môn

    @OneToMany(mappedBy = "instructor", fetch = FetchType.LAZY)
    private Set<ClassSchedule> classSchedules = new HashSet<>();
}