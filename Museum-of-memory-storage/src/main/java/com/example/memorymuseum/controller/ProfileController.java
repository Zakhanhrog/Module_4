package com.example.memorymuseum.controller;

import com.example.memorymuseum.dto.UserPasswordChangeDto;
import com.example.memorymuseum.dto.UserProfileUpdateDto;
import com.example.memorymuseum.model.User;
import com.example.memorymuseum.service.FileStorageService;
import com.example.memorymuseum.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Paths;
import java.security.Principal;

@Controller
@RequestMapping("/profile")
@PreAuthorize("isAuthenticated()")
public class ProfileController {

    private final UserService userService;
    private final FileStorageService fileStorageService;
    private static final String AVATAR_SUBFOLDER = "avatars";


    @Autowired
    public ProfileController(UserService userService, FileStorageService fileStorageService) {
        this.userService = userService;
        this.fileStorageService = fileStorageService;
    }

    private User getCurrentUser(Principal principal) {
        return userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database."));
    }

    @GetMapping
    public String viewProfile(Model model, Principal principal) {
        User currentUser = getCurrentUser(principal);
        model.addAttribute("user", currentUser);

        UserProfileUpdateDto profileUpdateDto = new UserProfileUpdateDto();
        profileUpdateDto.setFullName(currentUser.getFullName());
        profileUpdateDto.setEmail(currentUser.getEmail());
        model.addAttribute("profileUpdateDto", profileUpdateDto);

        model.addAttribute("passwordChangeDto", new UserPasswordChangeDto());
        return "profile/view";
    }

    @PostMapping("/update")
    public String updateProfile(@Valid @ModelAttribute("profileUpdateDto") UserProfileUpdateDto profileUpdateDto,
                                BindingResult result, Principal principal, RedirectAttributes redirectAttributes, Model model) {
        User currentUser = getCurrentUser(principal);
        if (result.hasErrors()) {
            model.addAttribute("user", currentUser);
            model.addAttribute("passwordChangeDto", new UserPasswordChangeDto()); // Cần để form đổi mk không lỗi
            return "profile/view";
        }
        try {
            userService.updateProfile(principal.getName(), profileUpdateDto);
            redirectAttributes.addFlashAttribute("successMessage", "Hồ sơ đã được cập nhật."); // I18N
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute("passwordChangeDto") UserPasswordChangeDto passwordChangeDto,
                                 BindingResult result, Principal principal, RedirectAttributes redirectAttributes, Model model) {
        User currentUser = getCurrentUser(principal);
        if (!passwordChangeDto.getNewPassword().equals(passwordChangeDto.getConfirmNewPassword())) {
            result.rejectValue("confirmNewPassword", "password.mismatch", "Mật khẩu mới không khớp."); // I18N
        }
        if (result.hasErrors()) {
            model.addAttribute("user", currentUser);
            // Cần tạo lại profileUpdateDto để form kia không lỗi
            UserProfileUpdateDto profileUpdateDto = new UserProfileUpdateDto();
            profileUpdateDto.setFullName(currentUser.getFullName());
            profileUpdateDto.setEmail(currentUser.getEmail());
            model.addAttribute("profileUpdateDto", profileUpdateDto);
            return "profile/view";
        }
        try {
            userService.changePassword(principal.getName(), passwordChangeDto);
            redirectAttributes.addFlashAttribute("successMessage", "Mật khẩu đã được thay đổi."); // I18N
        } catch (IllegalArgumentException | BadCredentialsException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/avatar/upload")
    public String uploadAvatar(@RequestParam("avatarFile") MultipartFile avatarFile,
                               Principal principal, RedirectAttributes redirectAttributes) {
        if (avatarFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn một file ảnh."); // I18N
            return "redirect:/profile";
        }
        try {
            userService.updateUserAvatar(principal.getName(), avatarFile);
            redirectAttributes.addFlashAttribute("successMessage", "Ảnh đại diện đã được cập nhật."); // I18N
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi tải lên ảnh đại diện."); // I18N
            e.printStackTrace();
        }
        return "redirect:/profile";
    }

    // Endpoint để hiển thị ảnh avatar (tương tự như file memory)
    @GetMapping("/avatar/{userId}/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveAvatar(@PathVariable Long userId, @PathVariable String filename) {
        Resource file = fileStorageService.loadAsResource(filename, AVATAR_SUBFOLDER + "/" + userId);
        if (file == null || !file.exists() || !file.isReadable()) {
            try {
                Resource defaultAvatar = fileStorageService.loadAsResource("default-avatar.png", "images"); // Giả sử có default-avatar.png trong static/images
                if(defaultAvatar.exists() || defaultAvatar.isReadable()) return ResponseEntity.ok().body(defaultAvatar);
            } catch (Exception e) {
                //ignore
            }
            return ResponseEntity.notFound().build();

        }
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename=\"" + file.getFilename() + "\"").body(file);
    }
}