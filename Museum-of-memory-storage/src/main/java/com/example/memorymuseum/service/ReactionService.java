package com.example.memorymuseum.service;

import com.example.memorymuseum.model.Memory;
import com.example.memorymuseum.model.ReactionType;
import com.example.memorymuseum.model.User;

import java.util.Map;
import java.util.Optional;

public interface ReactionService {
    boolean toggleReaction(Memory memory, User user, ReactionType reactionType);
    Map<ReactionType, Long> getReactionCountsForMemory(Memory memory);
    Optional<ReactionType> getUserReactionForMemory(Memory memory, User user); // Lấy reaction hiện tại của user
}