package com.artflowstudio.service;

import com.artflowstudio.dto.GradeDto;
import com.artflowstudio.entity.Grade;
import java.util.Optional;

public interface GradeService {
    Optional<Grade> getGradeByEnrollmentId(Long enrollmentId);
    Grade saveOrUpdateGrade(GradeDto gradeDto);
}