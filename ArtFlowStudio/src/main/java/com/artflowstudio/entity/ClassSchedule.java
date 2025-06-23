package com.artflowstudio.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "class_schedules")
@Getter
@Setter
@NoArgsConstructor
public class ClassSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Khóa học không được để trống")
    @ManyToOne(fetch = FetchType.LAZY) // Mối quan hệ nhiều-một với Course
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY) // Mối quan hệ nhiều-một với Instructor (có thể null nếu không có giảng viên cụ thể)
    @JoinColumn(name = "instructor_id")
    private Instructor instructor;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "Giờ bắt đầu không được để trống")
    private LocalTime startTime;

    @NotNull(message = "Giờ kết thúc không được để trống")
    private LocalTime endTime;

    @Column(length = 100) // Ví dụ: "Thứ 2, Thứ 4, Thứ 6" hoặc "T2-T4-T6"
    private String daysOfWeek;

    @NotNull(message = "Số buổi học không được để trống")
    @Min(value = 1, message = "Số buổi học phải ít nhất là 1")
    private Integer numberOfSessions;

    @NotNull(message = "Số lượng học viên tối đa không được để trống")
    @Min(value = 1, message = "Số lượng học viên tối đa phải ít nhất là 1")
    private Integer maxStudents;

    @Min(value = 0)
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer currentStudents = 0; // Số học viên hiện tại đã đăng ký

    @OneToMany(mappedBy = "classSchedule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<BookingRequest> bookingRequests = new HashSet<>();

    @OneToMany(mappedBy = "classSchedule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Enrollment> enrollments = new HashSet<>();

    public int getAvailableSlots() {
        if (maxStudents == null) return 0; // Hoặc một giá trị mặc định nào đó
        return maxStudents - currentStudents;
    }
}