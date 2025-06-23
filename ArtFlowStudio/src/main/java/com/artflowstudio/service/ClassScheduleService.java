package com.artflowstudio.service;

import com.artflowstudio.entity.ClassSchedule;
import java.util.List;
import java.util.Optional;

public interface ClassScheduleService {
    List<ClassSchedule> findAllSchedules();
    Optional<ClassSchedule> findScheduleById(Long id);
    List<ClassSchedule> findSchedulesByCourseId(Long courseId);
    // Các phương thức cho Admin (save, delete) sẽ thêm sau
}