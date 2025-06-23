package com.artflowstudio.service;

import com.artflowstudio.dto.ClassScheduleDto; // Sẽ tạo DTO này
import com.artflowstudio.entity.ClassSchedule;
import java.util.List;
import java.util.Optional;

public interface ClassScheduleService {
    List<ClassSchedule> findAllSchedules();
    List<ClassSchedule> findAllSchedulesForAdmin();
    Optional<ClassSchedule> findScheduleById(Long id);
    List<ClassSchedule> findSchedulesByCourseId(Long courseId);

    ClassSchedule saveSchedule(ClassScheduleDto classScheduleDto);
    ClassSchedule updateSchedule(Long id, ClassScheduleDto classScheduleDto);
    void deleteSchedule(Long id);
}