package com.example.memorymuseum.repository;

import com.example.memorymuseum.model.EmotionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmotionTypeRepository extends JpaRepository<EmotionType, Integer> {
    Optional<EmotionType> findByName(String name);
}