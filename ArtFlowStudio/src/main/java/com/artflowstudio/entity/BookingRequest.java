package com.artflowstudio.entity;

import com.artflowstudio.enums.BookingStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "booking_requests")
@Getter
@Setter
@NoArgsConstructor
public class BookingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Họ tên người học không được để trống")
    @Column(nullable = false, length = 100)
    private String fullName;

    private Integer age;

    @NotEmpty(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Column(nullable = false, length = 100)
    private String email;

    @NotEmpty(message = "Số điện thoại không được để trống")
    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String message; // Lời nhắn

    @NotNull(message = "Lịch học không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_schedule_id", nullable = false)
    private ClassSchedule classSchedule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status = BookingStatus.PENDING; // Mặc định là "Chờ duyệt"

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime requestDate;

    @PrePersist // Hàm này sẽ được gọi trước khi entity được lưu vào DB
    protected void onCreate() {
        requestDate = LocalDateTime.now();
    }
}