package com.artflowstudio.repository;

import com.artflowstudio.entity.BookingRequest;
import com.artflowstudio.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRequestRepository extends JpaRepository<BookingRequest, Long> {

    @Query("SELECT br FROM BookingRequest br " +
            "JOIN FETCH br.classSchedule cs " +
            "JOIN FETCH cs.course c " +
            "WHERE br.status = :status ORDER BY br.requestDate DESC")
    List<BookingRequest> findByStatusWithDetails(@Param("status") BookingStatus status);

    List<BookingRequest> findByClassScheduleId(Long classScheduleId);
    List<BookingRequest> findByEmail(String email);
}