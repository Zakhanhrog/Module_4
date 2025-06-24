package com.artflowstudio.controller;

import com.artflowstudio.dto.PasswordChangeDto;
import com.artflowstudio.entity.Enrollment;
import com.artflowstudio.entity.User;
import com.artflowstudio.exception.ResourceNotFoundException;
import com.artflowstudio.service.EnrollmentService;
import com.artflowstudio.service.GradeService;
import com.artflowstudio.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/learner")
public class LearnerController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final EnrollmentService enrollmentService;
    private final GradeService gradeService;

    @Autowired
    public LearnerController(UserService userService,
                             PasswordEncoder passwordEncoder,
                             EnrollmentService enrollmentService,
                             GradeService gradeService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.enrollmentService = enrollmentService;
        this.gradeService = gradeService;
    }

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResourceNotFoundException("No authenticated user found.");
        }
        String currentUsername = authentication.getName();
        return userService.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUsername));
    }

    @GetMapping("/profile")
    public String viewProfile(Model model) {
        User learner = getCurrentAuthenticatedUser();
        model.addAttribute("learner", learner);
        model.addAttribute("pageTitle", "Thông tin cá nhân");
        return "learner/profile";
    }

    @GetMapping("/change-password")
    public String changePasswordForm(Model model) {
        model.addAttribute("passwordChangeDto", new PasswordChangeDto());
        model.addAttribute("pageTitle", "Đổi mật khẩu");
        return "learner/change-password";
    }

    @PostMapping("/change-password")
    public String processChangePassword(@Valid @ModelAttribute("passwordChangeDto") PasswordChangeDto passwordChangeDto,
                                        BindingResult result,
                                        RedirectAttributes redirectAttributes, Model model) {
        User learner = getCurrentAuthenticatedUser();

        if (!passwordEncoder.matches(passwordChangeDto.getCurrentPassword(), learner.getPassword())) {
            result.rejectValue("currentPassword", "error.passwordChangeDto", "Mật khẩu hiện tại không đúng.");
        }

        if (passwordChangeDto.getNewPassword() != null && !passwordChangeDto.getNewPassword().equals(passwordChangeDto.getConfirmNewPassword())) {
            result.rejectValue("confirmNewPassword", "error.passwordChangeDto", "Xác nhận mật khẩu mới không khớp.");
        }

        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Đổi mật khẩu - Lỗi");
            return "learner/change-password";
        }
        userService.changePassword(learner, passwordChangeDto.getNewPassword());
        redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
        return "redirect:/learner/profile";
    }

    @GetMapping("/my-schedule")
    public String mySchedule(Model model) {
        User learner = getCurrentAuthenticatedUser();
        List<Enrollment> enrollments = enrollmentService.findEnrollmentsWithDetailsByUserId(learner.getId());
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("pageTitle", "Các Lớp Học Đã Đăng Ký");
        return "learner/my-schedule";
    }

    @GetMapping("/my-grades")
    public String myGrades(Model model) {
        User learner = getCurrentAuthenticatedUser();
        List<Enrollment> enrollments = enrollmentService.findEnrollmentsWithDetailsByUserId(learner.getId());
        model.addAttribute("enrollmentsWithGrades", enrollments);
        model.addAttribute("pageTitle", "Điểm số & Đánh giá");
        return "learner/my-grades";
    }
}