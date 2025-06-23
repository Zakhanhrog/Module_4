package com.artflowstudio.service.impl;

import com.artflowstudio.entity.ClassSchedule;
import com.artflowstudio.repository.ClassScheduleRepository;
import com.artflowstudio.service.ClassScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ClassScheduleServiceImpl implements ClassScheduleService {

    private final ClassScheduleRepository classScheduleRepository;

    @Autowired
    public ClassScheduleServiceImpl(ClassScheduleRepository classScheduleRepository) {
        this.classScheduleRepository = classScheduleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassSchedule> findAllSchedules() {
        return classScheduleRepository.findAllUpcomingSchedulesWithDetails(LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClassSchedule> findScheduleById(Long id) {
        return classScheduleRepository.findByIdWithDetails(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassSchedule> findSchedulesByCourseId(Long courseId) {
        return classScheduleRepository.findByCourseIdWithCourse(courseId);
    }
}