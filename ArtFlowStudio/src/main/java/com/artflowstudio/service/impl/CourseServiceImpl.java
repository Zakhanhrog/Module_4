package com.artflowstudio.service.impl;

import com.artflowstudio.entity.Course;
import com.artflowstudio.exception.ResourceNotFoundException;
import com.artflowstudio.repository.CourseRepository;
import com.artflowstudio.service.CourseService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final Path imageStorageLocation;

    public CourseServiceImpl(CourseRepository courseRepository,
                             @Value("${file.upload-dir:src/main/resources/static/images/courses}") String uploadDir) {
        this.courseRepository = courseRepository;
        this.imageStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.imageStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> findAllCourses() {
        return courseRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Course> findCourseById(Long id) {
        return courseRepository.findById(id);
    }

    @Override
    @Transactional
    public Course saveCourse(Course course, MultipartFile imageFile) throws IOException {
        String originalFileName = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            originalFileName = StringUtils.cleanPath(imageFile.getOriginalFilename());
            if (originalFileName.contains("..")) {
                throw new IOException("Sorry! Filename contains invalid path sequence " + originalFileName);
            }

            String fileExtension = "";
            try {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            } catch (Exception e) {
                fileExtension = "";
            }
            String newFileName = UUID.randomUUID().toString() + fileExtension;
            Path targetLocation = this.imageStorageLocation.resolve(newFileName);

            try (InputStream inputStream = imageFile.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }
            course.setImageUrl("/images/courses/" + newFileName);
        } else if (course.getId() != null) { // Trường hợp edit mà không upload ảnh mới
            Course existingCourse = courseRepository.findById(course.getId()).orElse(null);
            if (existingCourse != null && (course.getImageUrl() == null || course.getImageUrl().isEmpty())) {
                course.setImageUrl(existingCourse.getImageUrl()); // Giữ lại ảnh cũ
            }
        }
        return courseRepository.save(course);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) throws IOException {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        if (course.getImageUrl() != null && !course.getImageUrl().isEmpty()) {
            try {
                String fileName = course.getImageUrl().substring(course.getImageUrl().lastIndexOf("/") + 1);
                Path filePath = this.imageStorageLocation.resolve(fileName).normalize();
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
            } catch (IOException ex) {
                System.err.println("Could not delete image file for course " + id + ": " + ex.getMessage());
                // Quyết định có ném exception ở đây hay không, hoặc chỉ log
            }
        }
        courseRepository.deleteById(id);
    }
}