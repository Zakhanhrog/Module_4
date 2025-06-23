package com.artflowstudio.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "grades")
@Getter
@Setter
@NoArgsConstructor
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY) // Mối quan hệ một-một với Enrollment
    @JoinColumn(name = "enrollment_id", nullable = false, unique = true) // Mỗi enrollment chỉ có 1 grade
    private Enrollment enrollment;

    private Double score; // Điểm số

    @Lob
    @Column(columnDefinition = "TEXT")
    private String feedback; // Nhận xét, đánh giá

    @Temporal(TemporalType.DATE)
    private LocalDate evaluationDate; // Ngày đánh giá

    @PrePersist
    protected void onCreate() {
        if (evaluationDate == null) {
            evaluationDate = LocalDate.now();
        }
    }
}