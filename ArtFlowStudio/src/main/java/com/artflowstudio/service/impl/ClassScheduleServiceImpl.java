package com.artflowstudio.service.impl;

import com.artflowstudio.dto.ClassScheduleDto;
import com.artflowstudio.entity.ClassSchedule;
import com.artflowstudio.entity.Course;
import com.artflowstudio.entity.Instructor;
import com.artflowstudio.exception.ResourceNotFoundException;
import com.artflowstudio.repository.ClassScheduleRepository;
import com.artflowstudio.repository.CourseRepository;
import com.artflowstudio.repository.InstructorRepository;
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
    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository; // Inject InstructorRepository

    @Autowired
    public ClassScheduleServiceImpl(ClassScheduleRepository classScheduleRepository,
                                    CourseRepository courseRepository,
                                    InstructorRepository instructorRepository) {
        this.classScheduleRepository = classScheduleRepository;
        this.courseRepository = courseRepository;
        this.instructorRepository = instructorRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassSchedule> findAllSchedules() {
        return classScheduleRepository.findAllUpcomingSchedulesWithDetails(LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassSchedule> findAllSchedulesForAdmin() {
        // Admin có thể muốn xem tất cả lịch, kể cả lịch cũ
        // Cần một query khác trong repository nếu muốn fetch details, ví dụ:
        // return classScheduleRepository.findAllWithDetails();
        // Hoặc đơn giản là:
        return classScheduleRepository.findAll(); // Sẽ lazy load nếu truy cập course/instructor trong view
        // Để đơn giản, tạm thời dùng findAll(), sẽ tối ưu sau nếu cần
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<ClassSchedule> findScheduleById(Long id) {
        return classScheduleRepository.findByIdWithDetails(id); // Dùng query có JOIN FETCH
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassSchedule> findSchedulesByCourseId(Long courseId) {
        return classScheduleRepository.findByCourseIdWithCourse(courseId); // Dùng query có JOIN FETCH
    }

    @Override
    @Transactional
    public ClassSchedule saveSchedule(ClassScheduleDto dto) {
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + dto.getCourseId()));

        Instructor instructor = null;
        if (dto.getInstructorId() != null) {
            instructor = instructorRepository.findById(dto.getInstructorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Instructor not found with id: " + dto.getInstructorId()));
        }

        ClassSchedule schedule = new ClassSchedule();
        mapDtoToEntity(dto, schedule, course, instructor);
        return classScheduleRepository.save(schedule);
    }

    @Override
    @Transactional
    public ClassSchedule updateSchedule(Long id, ClassScheduleDto dto) {
        ClassSchedule schedule = classScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ClassSchedule not found with id: " + id));

        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + dto.getCourseId()));

        Instructor instructor = null;
        if (dto.getInstructorId() != null) {
            instructor = instructorRepository.findById(dto.getInstructorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Instructor not found with id: " + dto.getInstructorId()));
        }

        mapDtoToEntity(dto, schedule, course, instructor);
        return classScheduleRepository.save(schedule);
    }

    private void mapDtoToEntity(ClassScheduleDto dto, ClassSchedule entity, Course course, Instructor instructor) {
        entity.setCourse(course);
        entity.setInstructor(instructor);
        entity.setStartDate(dto.getStartDate());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setDaysOfWeek(dto.getDaysOfWeek());
        entity.setNumberOfSessions(dto.getNumberOfSessions());
        entity.setMaxStudents(dto.getMaxStudents());
    }


    @Override
    @Transactional
    public void deleteSchedule(Long id) {
        if (!classScheduleRepository.existsById(id)) {
            throw new ResourceNotFoundException("ClassSchedule not found with id: " + id);
        }
        classScheduleRepository.deleteById(id);
    }
}