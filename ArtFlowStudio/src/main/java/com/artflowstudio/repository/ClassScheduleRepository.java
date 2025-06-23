package com.artflowstudio.repository;

import com.artflowstudio.entity.ClassSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, Long> {

    List<ClassSchedule> findByCourseId(Long courseId);

    @Query("SELECT cs FROM ClassSchedule cs WHERE cs.startDate >= :today ORDER BY cs.startDate, cs.startTime")
    List<ClassSchedule> findAllUpcomingSchedules(LocalDate today);

    List<ClassSchedule> findByInstructorId(Long instructorId);

}