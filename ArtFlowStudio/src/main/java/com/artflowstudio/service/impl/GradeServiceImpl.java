package com.artflowstudio.service.impl;

import com.artflowstudio.dto.GradeDto;
import com.artflowstudio.entity.Enrollment;
import com.artflowstudio.entity.Grade;
import com.artflowstudio.exception.ResourceNotFoundException;
import com.artflowstudio.repository.EnrollmentRepository;
import com.artflowstudio.repository.GradeRepository;
import com.artflowstudio.service.GradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Autowired
    public GradeServiceImpl(GradeRepository gradeRepository, EnrollmentRepository enrollmentRepository) {
        this.gradeRepository = gradeRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Grade> getGradeByEnrollmentId(Long enrollmentId) {
        return gradeRepository.findByEnrollmentId(enrollmentId);
    }

    @Override
    @Transactional
    public Grade saveOrUpdateGrade(GradeDto gradeDto) {
        Enrollment enrollment = enrollmentRepository.findById(gradeDto.getEnrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + gradeDto.getEnrollmentId()));

        Grade grade = gradeRepository.findByEnrollmentId(enrollment.getId())
                .orElse(new Grade());

        grade.setEnrollment(enrollment);
        grade.setScore(gradeDto.getScore());
        grade.setFeedback(gradeDto.getFeedback());
        if (grade.getId() == null) {
            grade.setEvaluationDate(LocalDate.now());
        }

        return gradeRepository.save(grade);
    }
}