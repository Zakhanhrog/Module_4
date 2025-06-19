package com.example.memorymuseum.service;

import com.example.memorymuseum.model.Comment;
import com.example.memorymuseum.model.Memory;
import com.example.memorymuseum.model.User;

import java.util.List;
import java.util.Optional;

public interface CommentService {
    Comment addComment(Memory memory, User user, String content, Long parentCommentId);
    List<Comment> getCommentsForMemory(Memory memory); // Lấy các bình luận gốc
    void deleteComment(Long commentId, User currentUser);
    Optional<Comment> findById(Long commentId);

}