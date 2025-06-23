package com.artflowstudio.repository;

import com.artflowstudio.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    Optional<Grade> findByEnrollmentId(Long enrollmentId);
    List<Grade> findByEnrollment_UserId(Long userId);
    List<Grade> findByEnrollment_ClassSchedule_CourseId(Long courseId);
}