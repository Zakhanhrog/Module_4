package com.artflowstudio.service.impl;

import com.artflowstudio.entity.Instructor;
import com.artflowstudio.exception.ResourceNotFoundException;
import com.artflowstudio.repository.InstructorRepository;
import com.artflowstudio.service.InstructorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class InstructorServiceImpl implements InstructorService {

    private final InstructorRepository instructorRepository;

    @Autowired
    public InstructorServiceImpl(InstructorRepository instructorRepository) {
        this.instructorRepository = instructorRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Instructor> findAllInstructors() {
        return instructorRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Instructor> findById(Long id) {
        return instructorRepository.findById(id);
    }

    @Override
    @Transactional
    public Instructor saveInstructor(Instructor instructor) {
        return instructorRepository.save(instructor);
    }

    @Override
    @Transactional
    public void deleteInstructor(Long id) {
        if(!instructorRepository.existsById(id)){
            throw new ResourceNotFoundException("Instructor not found with id: " + id);
        }
        instructorRepository.deleteById(id);
    }
}