package com.example.memorymuseum.service;

import com.example.memorymuseum.model.EmotionType;

import java.util.List;
import java.util.Optional;

public interface EmotionTypeService {
    List<EmotionType> findAll();
    Optional<EmotionType> findById(Integer id);
    EmotionType save(EmotionType emotionType);
    void initDefaultEmotions();
}