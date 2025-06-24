package com.artflowstudio.repository;

import com.artflowstudio.entity.Enrollment;
import com.artflowstudio.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Optional<Enrollment> findByUserAndClassScheduleId(User user, Long classScheduleId);
    List<Enrollment> findByUserId(Long userId);
    List<Enrollment> findByClassScheduleId(Long classScheduleId);

    @Query("SELECT e FROM Enrollment e " +
            "JOIN FETCH e.user u " +
            "JOIN FETCH e.classSchedule cs " +
            "JOIN FETCH cs.course c " +
            "LEFT JOIN FETCH cs.instructor i " +
            "WHERE e.user.id = :userId AND cs.startDate >= :currentDate " +
            "ORDER BY cs.startDate ASC, cs.startTime ASC")
    List<Enrollment> findActiveEnrollmentsForUser(@Param("userId") Long userId, @Param("currentDate") LocalDate currentDate);

    @Query("SELECT e FROM Enrollment e " +
            "JOIN FETCH e.user u " +
            "JOIN FETCH e.classSchedule cs " +
            "JOIN FETCH cs.course c " +
            "LEFT JOIN FETCH cs.instructor i " +
            "WHERE (:userId IS NULL OR e.user.id = :userId) " +
            "ORDER BY cs.startDate DESC, u.fullName ASC")
    List<Enrollment> findEnrollmentsWithDetailsByUserId(@Param("userId") Long userId);

    @Query("SELECT e FROM Enrollment e " +
            "JOIN FETCH e.user u " +
            "JOIN FETCH e.classSchedule cs " +
            "JOIN FETCH cs.course c " +
            "LEFT JOIN FETCH cs.instructor i " +
            "ORDER BY cs.startDate DESC, u.fullName ASC")
    List<Enrollment> findAllEnrollmentsWithDetails();

    @Query("SELECT e FROM Enrollment e " +
            "JOIN FETCH e.user u " +
            "JOIN FETCH e.classSchedule cs " +
            "JOIN FETCH cs.course c " +
            "LEFT JOIN FETCH cs.instructor i " +
            "WHERE e.id = :enrollmentId")
    Optional<Enrollment> findByIdWithDetails(@Param("enrollmentId") Long enrollmentId);
}