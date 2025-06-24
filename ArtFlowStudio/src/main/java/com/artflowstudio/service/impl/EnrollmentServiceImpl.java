package com.artflowstudio.service.impl;

import com.artflowstudio.entity.Enrollment;
import com.artflowstudio.repository.EnrollmentRepository;
import com.artflowstudio.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;

    @Autowired
    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Enrollment> findAllEnrollmentsForGrading() {
        return enrollmentRepository.findAllEnrollmentsWithDetails();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Enrollment> findEnrollmentByIdWithDetails(Long enrollmentId) {
        return enrollmentRepository.findByIdWithDetails(enrollmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Enrollment> findEnrollmentsWithDetailsByUserId(Long userId) {
        return enrollmentRepository.findEnrollmentsWithDetailsByUserId(userId);
    }
}