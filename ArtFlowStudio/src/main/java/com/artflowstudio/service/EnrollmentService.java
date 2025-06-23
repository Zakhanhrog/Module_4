package com.artflowstudio.service;

import com.artflowstudio.entity.Enrollment;
import java.util.List;
import java.util.Optional;

public interface EnrollmentService {
    List<Enrollment> findAllEnrollmentsForGrading();
    Optional<Enrollment> findEnrollmentByIdWithDetails(Long enrollmentId);
}