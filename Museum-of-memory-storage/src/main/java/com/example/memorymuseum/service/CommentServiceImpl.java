package com.example.memorymuseum.service;

import com.example.memorymuseum.model.Comment;
import com.example.memorymuseum.model.Memory;
import com.example.memorymuseum.model.User;
import com.example.memorymuseum.repository.CommentRepository;
import com.example.memorymuseum.repository.MemoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final MemoryRepository memoryRepository; // Cần để kiểm tra Memory tồn tại

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository, MemoryRepository memoryRepository) {
        this.commentRepository = commentRepository;
        this.memoryRepository = memoryRepository;
    }

    @Override
    @Transactional
    public Comment addComment(Memory memory, User user, String content, Long parentCommentId) {
        if (memory == null || user == null || content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Memory, user, and content cannot be null or empty.");
        }

        // Kiểm tra memory có tồn tại không (nếu cần thiết, thường thì đối tượng memory đã được lấy từ DB)
        // memoryRepository.findById(memory.getId()).orElseThrow(() -> new IllegalArgumentException("Memory not found"));

        Comment comment = new Comment();
        comment.setMemory(memory);
        comment.setUser(user);
        comment.setContent(content.trim());

        if (parentCommentId != null) {
            Comment parent = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent comment not found with ID: " + parentCommentId));
            if (!parent.getMemory().getId().equals(memory.getId())) {
                throw new IllegalArgumentException("Parent comment does not belong to the same memory.");
            }
            comment.setParentComment(parent);
        }
        return commentRepository.save(comment);
    }

    @Override
    public List<Comment> getCommentsForMemory(Memory memory) {
        return commentRepository.findByMemoryAndParentCommentIsNullOrderByCreatedAtAsc(memory);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, User currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found with ID: " + commentId));

        boolean canDelete = currentUser.getId().equals(comment.getUser().getId()) ||
                currentUser.getRole() == com.example.memorymuseum.model.Role.ADMIN ||
                currentUser.getId().equals(comment.getMemory().getUser().getId());

        if (!canDelete) {
            throw new AccessDeniedException("You are not authorized to delete this comment.");
        }
        commentRepository.delete(comment);
    }
    @Override
    public Optional<Comment> findById(Long commentId) {
        return commentRepository.findById(commentId);
    }
}