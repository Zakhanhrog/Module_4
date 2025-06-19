package com.example.memorymuseum.controller;

import com.example.memorymuseum.model.Memory;
import com.example.memorymuseum.model.ReactionType;
import com.example.memorymuseum.model.User;
import com.example.memorymuseum.service.MemoryService;
import com.example.memorymuseum.service.ReactionService;
import com.example.memorymuseum.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/reactions")
public class ReactionController {

    private final ReactionService reactionService;
    private final MemoryService memoryService;
    private final UserService userService;

    @Autowired
    public ReactionController(ReactionService reactionService, MemoryService memoryService, UserService userService) {
        this.reactionService = reactionService;
        this.memoryService = memoryService;
        this.userService = userService;
    }

    @PostMapping("/toggle/{memoryId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> toggleReaction(@PathVariable Long memoryId, @RequestParam String reactionTypeString) {
        User currentUser = userService.getCurrentUser().orElse(null);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not authenticated."));
        }

        Memory memory = memoryService.findById(memoryId).orElse(null);
        if (memory == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Memory not found."));
        }

        ReactionType reactionType;
        try {
            reactionType = ReactionType.valueOf(reactionTypeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid reaction type."));
        }

        boolean reactionStateChanged = reactionService.toggleReaction(memory, currentUser, reactionType);
        Optional<ReactionType> userCurrentReaction = reactionService.getUserReactionForMemory(memory, currentUser);


        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("reactionType", reactionType.name());
        response.put("userReaction", userCurrentReaction.map(Enum::name).orElse(null)); // Trạng thái reaction của user sau khi toggle
        response.put("newCounts", reactionService.getReactionCountsForMemory(memory));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/memory/{memoryId}")
    public ResponseEntity<?> getReactionsForMemory(@PathVariable Long memoryId) {
        Memory memory = memoryService.findById(memoryId).orElse(null);
        if (memory == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Memory not found."));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("counts", reactionService.getReactionCountsForMemory(memory));

        Optional<User> currentUserOpt = userService.getCurrentUser();
        if (currentUserOpt.isPresent()) {
            Optional<ReactionType> userReaction = reactionService.getUserReactionForMemory(memory, currentUserOpt.get());
            response.put("userReaction", userReaction.map(Enum::name).orElse(null));
        } else {
            response.put("userReaction", null);
        }
        return ResponseEntity.ok(response);
    }
}