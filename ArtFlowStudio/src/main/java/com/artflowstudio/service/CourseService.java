package com.artflowstudio.service;

import com.artflowstudio.entity.Course;
import org.springframework.web.multipart.MultipartFile; // Sẽ dùng cho Admin sau
import java.util.List;
import java.util.Optional;

public interface CourseService {
    List<Course> findAllCourses();
    Optional<Course> findCourseById(Long id);
}