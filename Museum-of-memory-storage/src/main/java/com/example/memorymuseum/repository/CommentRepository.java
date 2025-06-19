package com.example.memorymuseum.repository;

import com.example.memorymuseum.model.Comment;
import com.example.memorymuseum.model.Memory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByMemoryOrderByCreatedAtDesc(Memory memory);
    List<Comment> findByMemoryAndParentCommentIsNullOrderByCreatedAtAsc(Memory memory); // Lấy bình luận gốc, sắp xếp tăng dần để hiển thị từ cũ đến mới
}