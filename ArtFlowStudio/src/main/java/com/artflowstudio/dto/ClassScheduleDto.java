package com.artflowstudio.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
public class ClassScheduleDto {
    private Long id;

    @NotNull(message = "Khóa học không được để trống")
    private Long courseId;

    private Long instructorId; // Có thể null

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "Giờ bắt đầu không được để trống")
    @DateTimeFormat(pattern = "HH:mm") // Quan trọng: pattern cho LocalTime
    private LocalTime startTime;

    @NotNull(message = "Giờ kết thúc không được để trống")
    @DateTimeFormat(pattern = "HH:mm") // Quan trọng: pattern cho LocalTime
    private LocalTime endTime;

    private String daysOfWeek; // Ví dụ: "Thứ 2, Thứ 4, Thứ 6"

    @NotNull(message = "Số buổi học không được để trống")
    @Min(value = 1, message = "Số buổi học phải ít nhất là 1")
    private Integer numberOfSessions;

    @NotNull(message = "Số lượng học viên tối đa không được để trống")
    @Min(value = 1, message = "Số lượng học viên tối đa phải ít nhất là 1")
    private Integer maxStudents;

    private Integer currentStudents = 0; // Thường không set từ form, sẽ tự tính
}