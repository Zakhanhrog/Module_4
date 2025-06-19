package com.example.memorymuseum.repository;

import com.example.memorymuseum.model.Memory;
import com.example.memorymuseum.model.Reaction;
import com.example.memorymuseum.model.ReactionType;
import com.example.memorymuseum.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    Optional<Reaction> findByMemoryAndUser(Memory memory, User user);
    long countByMemoryAndReactionType(Memory memory, ReactionType reactionType);
}