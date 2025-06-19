package com.example.memorymuseum.service;

import com.example.memorymuseum.model.Memory;
import com.example.memorymuseum.model.Reaction;
import com.example.memorymuseum.model.ReactionType;
import com.example.memorymuseum.model.User;
import com.example.memorymuseum.repository.ReactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ReactionServiceImpl implements ReactionService {

    private final ReactionRepository reactionRepository;

    @Autowired
    public ReactionServiceImpl(ReactionRepository reactionRepository) {
        this.reactionRepository = reactionRepository;
    }

    @Override
    @Transactional
    public boolean toggleReaction(Memory memory, User user, ReactionType reactionType) {
        Optional<Reaction> existingUserReactionOpt = reactionRepository.findByMemoryAndUser(memory, user);

        if (existingUserReactionOpt.isPresent()) {
            Reaction existingReaction = existingUserReactionOpt.get();
            if (existingReaction.getReactionType() == reactionType) {
                reactionRepository.delete(existingReaction);
                return false; // Reaction removed
            } else {
                existingReaction.setReactionType(reactionType);
                reactionRepository.save(existingReaction);
                return true; // Reaction updated
            }
        } else {
            Reaction newReaction = new Reaction();
            newReaction.setMemory(memory);
            newReaction.setUser(user);
            newReaction.setReactionType(reactionType);
            reactionRepository.save(newReaction);
            return true; // Reaction added
        }
    }

    @Override
    public Map<ReactionType, Long> getReactionCountsForMemory(Memory memory) {
        Map<ReactionType, Long> counts = new EnumMap<>(ReactionType.class);
        for (ReactionType type : ReactionType.values()) {
            counts.put(type, reactionRepository.countByMemoryAndReactionType(memory, type));
        }
        return counts;
    }

    @Override
    public Optional<ReactionType> getUserReactionForMemory(Memory memory, User user) {
        return reactionRepository.findByMemoryAndUser(memory, user)
                .map(Reaction::getReactionType);
    }
}