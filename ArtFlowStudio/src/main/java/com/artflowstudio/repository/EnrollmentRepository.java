package com.artflowstudio.repository;

import com.artflowstudio.entity.Enrollment;
import com.artflowstudio.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Optional<Enrollment> findByUserAndClassScheduleId(User user, Long classScheduleId);
    List<Enrollment> findByUserId(Long userId);
    List<Enrollment> findByClassScheduleId(Long classScheduleId);

    @Query("SELECT e FROM Enrollment e WHERE e.user.id = :userId AND e.classSchedule.startDate >= :currentDate ORDER BY e.classSchedule.startDate, e.classSchedule.startTime")
    List<Enrollment> findActiveEnrollmentsForUser(Long userId, LocalDate currentDate);

    @Query("SELECT e FROM Enrollment e JOIN FETCH e.classSchedule cs JOIN FETCH cs.course c WHERE e.user.id = :userId")
    List<Enrollment> findEnrollmentsWithDetailsByUserId(Long userId);
}