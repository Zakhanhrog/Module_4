package com.artflowstudio.service;

import com.artflowstudio.entity.Course;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;

public interface CourseService {
    List<Course> findAllCourses();
    Optional<Course> findCourseById(Long id);
    Course saveCourse(Course course, MultipartFile imageFile) throws Exception;
    void deleteCourse(Long id) throws Exception;
}