package com.artflowstudio.service;

import com.artflowstudio.entity.Instructor;
import java.util.List;
import java.util.Optional;

public interface InstructorService {
    List<Instructor> findAllInstructors();
    Optional<Instructor> findById(Long id);
    Instructor saveInstructor(Instructor instructor); // Dùng cho cả create và update
    void deleteInstructor(Long id);
}