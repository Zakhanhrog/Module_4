package com.example.memorymuseum.service;

import com.example.memorymuseum.model.EmotionType;
import com.example.memorymuseum.repository.EmotionTypeRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmotionTypeServiceImpl implements EmotionTypeService {

    private final EmotionTypeRepository emotionTypeRepository;

    public EmotionTypeServiceImpl(EmotionTypeRepository emotionTypeRepository) {
        this.emotionTypeRepository = emotionTypeRepository;
    }

    @Override
    public List<EmotionType> findAll() {
        return emotionTypeRepository.findAll();
    }

    @Override
    public Optional<EmotionType> findById(Integer id) {
        return emotionTypeRepository.findById(id);
    }

    @Override
    public EmotionType save(EmotionType emotionType) {
        return emotionTypeRepository.save(emotionType);
    }

    @Override
    @PostConstruct
    public void initDefaultEmotions() {
        if (emotionTypeRepository.count() == 0) {
            emotionTypeRepository.save(new EmotionType(null, "Vui", "Happy", "fa-smile"));
            emotionTypeRepository.save(new EmotionType(null, "Buồn", "Sad", "fa-sad-tear"));
            emotionTypeRepository.save(new EmotionType(null, "Tức giận", "Angry", "fa-angry"));
            emotionTypeRepository.save(new EmotionType(null, "Tự hào", "Proud", "fa-medal"));
            emotionTypeRepository.save(new EmotionType(null, "Ngạc nhiên", "Surprised", "fa-surprise"));
            emotionTypeRepository.save(new EmotionType(null, "Lo lắng", "Worried", "fa-meh-rolling-eyes"));
            emotionTypeRepository.save(new EmotionType(null, "Yêu thích", "Love", "fa-heart"));
            emotionTypeRepository.save(new EmotionType(null, "Bình thường", "Neutral", "fa-meh"));
        }
    }
}