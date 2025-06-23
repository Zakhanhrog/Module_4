package com.artflowstudio.repository;

import com.artflowstudio.entity.BookingRequest;
import com.artflowstudio.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRequestRepository extends JpaRepository<BookingRequest, Long> {
    List<BookingRequest> findByStatusOrderByRequestDateDesc(BookingStatus status);
    List<BookingRequest> findByClassScheduleId(Long classScheduleId);
    List<BookingRequest> findByEmail(String email);
}