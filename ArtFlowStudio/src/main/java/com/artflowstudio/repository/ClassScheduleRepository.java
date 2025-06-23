package com.artflowstudio.repository;

import com.artflowstudio.entity.ClassSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, Long> {

    @Query("SELECT cs FROM ClassSchedule cs JOIN FETCH cs.course c WHERE cs.course.id = :courseId ORDER BY cs.startDate, cs.startTime")
    List<ClassSchedule> findByCourseIdWithCourse(@Param("courseId") Long courseId);

    @Query("SELECT cs FROM ClassSchedule cs JOIN FETCH cs.course c LEFT JOIN FETCH cs.instructor i WHERE cs.startDate >= :today ORDER BY cs.startDate, cs.startTime")
    List<ClassSchedule> findAllUpcomingSchedulesWithDetails(@Param("today") LocalDate today);

    @Query("SELECT cs FROM ClassSchedule cs JOIN FETCH cs.course c LEFT JOIN FETCH cs.instructor i WHERE cs.instructor.id = :instructorId ORDER BY cs.startDate, cs.startTime")
    List<ClassSchedule> findByInstructorIdWithDetails(@Param("instructorId") Long instructorId);

    @Query("SELECT cs FROM ClassSchedule cs JOIN FETCH cs.course c LEFT JOIN FETCH cs.instructor i WHERE cs.id = :id")
    Optional<ClassSchedule> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT DISTINCT cs FROM ClassSchedule cs LEFT JOIN FETCH cs.course c LEFT JOIN FETCH cs.instructor i ORDER BY cs.startDate DESC, cs.id DESC")
    List<ClassSchedule> findAllWithDetailsForAdmin(); // PHƯƠNG THỨC MỚI
}