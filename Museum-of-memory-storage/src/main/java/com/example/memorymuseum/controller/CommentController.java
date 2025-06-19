package com.example.memorymuseum.controller;

import com.example.memorymuseum.model.Memory;
import com.example.memorymuseum.model.User;
import com.example.memorymuseum.service.CommentService;
import com.example.memorymuseum.service.MemoryService;
import com.example.memorymuseum.service.UserService;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.AccessDeniedException;

@Controller
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;
    private final MemoryService memoryService;
    private final UserService userService;

    @Autowired
    public CommentController(CommentService commentService, MemoryService memoryService, UserService userService) {
        this.commentService = commentService;
        this.memoryService = memoryService;
        this.userService = userService;
    }

    @PostMapping("/add/{memoryId}")
    @PreAuthorize("isAuthenticated()")
    public String addComment(@PathVariable("memoryId") Long memoryId,
                             @RequestParam("content") @NotEmpty String content,
                             @RequestParam(value = "parentId", required = false) Long parentId,
                             RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("User not authenticated for commenting."));
        Memory memory = memoryService.findById(memoryId)
                .orElseThrow(() -> new IllegalArgumentException("Memory not found for commenting: " + memoryId));

        try {
            commentService.addComment(memory, currentUser, content, parentId);
            redirectAttributes.addFlashAttribute("successMessage", "Bình luận đã được thêm."); // I18N sau
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/memories/" + memoryId + "#comment-section"; // Chuyển hướng về phần bình luận
    }

    @PostMapping("/delete/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public String deleteComment(@PathVariable("commentId") Long commentId,
                                RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("User not authenticated for deleting comment."));
        // Lấy memoryId để redirect lại đúng chỗ, cần tìm comment trước
        Long memoryId = commentService.findById(commentId) // Sử dụng service
                .map(c -> c.getMemory().getId())
                .orElse(null);
        if (memoryId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy bình luận hoặc ký ức liên quan.");
            return "redirect:/home"; // Hoặc một trang lỗi chung
        }

        try {
            commentService.deleteComment(commentId, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Bình luận đã được xóa."); // I18N sau
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/memories/" + memoryId + "#comment-section";
    }
}