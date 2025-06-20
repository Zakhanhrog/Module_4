package com.example.memorymuseum.controller;

import com.example.memorymuseum.dto.MemoryDto;
import com.example.memorymuseum.dto.MemorySearchDto;
import com.example.memorymuseum.dto.UserProfileUpdateDto;
import com.example.memorymuseum.dto.UserSearchDto;
import com.example.memorymuseum.model.LogAction;
import com.example.memorymuseum.model.Memory;
import com.example.memorymuseum.model.Role;
import com.example.memorymuseum.model.User;
import com.example.memorymuseum.service.LogActionService;
import com.example.memorymuseum.service.MemoryService;
import com.example.memorymuseum.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')") // Đảm bảo tất cả các method trong controller này cần quyền ADMIN
public class AdminController {

    private final UserService userService;
    private final MemoryService memoryService;
    private final LogActionService logActionService;

    @Autowired
    public AdminController(UserService userService, MemoryService memoryService, LogActionService logActionService) {
        this.userService = userService;
        this.memoryService = memoryService;
        this.logActionService = logActionService;
    }

    @GetMapping
    public String adminDashboard(Model model) {
        // Lấy một số thống kê cơ bản
        long totalUsers = userService.countTotalUsers();
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("pageTitle", "Admin Dashboard");
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String listUsers(Model model,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            @RequestParam(defaultValue = "id,asc") String[] sort) {

        String sortField = sort[0];
        String sortDirection = sort.length > 1 ? sort[1] : "asc";
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sortOrder = Sort.by(new Sort.Order(direction, sortField).ignoreCase()); // ignoreCase cho text sorting

        Pageable pageable = PageRequest.of(page, size, sortOrder);
        Page<User> userPage = userService.findAllUsers(pageable); // Cần thêm method này vào UserService

        model.addAttribute("userPage", userPage);
        model.addAttribute("pageTitle", "User Management");
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDirection);
        model.addAttribute("reverseSortDir", sortDirection.equals("asc") ? "desc" : "asc");

        return "admin/user-list";
    }

    @PostMapping("/users/{userId}/change-role")
    public String changeUserRole(@PathVariable Long userId,
                                 @RequestParam String role,
                                 Principal principal, // Để kiểm tra admin không tự đổi vai trò của chính mình
                                 RedirectAttributes redirectAttributes) {
        User currentUser = userService.findByUsername(principal.getName()).orElse(null);
        if (currentUser != null && currentUser.getId().equals(userId) && role.equalsIgnoreCase(Role.USER.name())) {
            // Ngăn admin tự hạ vai trò của chính mình thành USER (cần ít nhất 1 admin)
            // Logic này có thể cần phức tạp hơn để kiểm tra nếu là admin duy nhất
            redirectAttributes.addFlashAttribute("errorMessage", "Admin không thể tự hạ vai trò của chính mình.");
            return "redirect:/admin/users";
        }

        try {
            Role newRole = Role.valueOf(role.toUpperCase());
            userService.changeUserRole(userId, newRole);
            redirectAttributes.addFlashAttribute("successMessage", "Vai trò người dùng đã được cập nhật.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vai trò không hợp lệ hoặc người dùng không tồn tại.");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{userId}/toggle-status")
    public String toggleUserStatus(@PathVariable Long userId,
                                   Principal principal, // Để kiểm tra admin không tự khóa chính mình
                                   RedirectAttributes redirectAttributes) {
        User currentUser = userService.findByUsername(principal.getName()).orElse(null);
        User targetUser = userService.findById(userId).orElse(null);

        if (targetUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Người dùng không tồn tại.");
            return "redirect:/admin/users";
        }

        if (currentUser != null && currentUser.getId().equals(userId)) {
            // Ngăn admin tự khóa tài khoản của chính mình
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không thể tự khóa tài khoản của chính mình.");
            return "redirect:/admin/users";
        }
        // Ngăn chặn việc vô hiệu hóa admin cuối cùng (cần logic phức tạp hơn nếu muốn)

        try {
            userService.toggleUserEnabledStatus(userId);
            String status = targetUser.isEnabled() ? "mở khóa" : "khóa"; // Trạng thái sau khi toggle sẽ ngược lại
            if (!targetUser.isEnabled()){ // Vì hàm toggle đã đổi trạng thái, nên giờ nó là !targetUser.isEnabled()
                status = "khóa";
            } else {
                status = "mở khóa";
            }
            redirectAttributes.addFlashAttribute("successMessage", "Tài khoản người dùng đã được " + status + ".");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Người dùng không tồn tại.");
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{userId}/view")
    public String viewUserDetails(@PathVariable Long userId, Model model, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại với ID: " + userId));
            
            model.addAttribute("user", user);
            model.addAttribute("pageTitle", "Chi tiết người dùng");
            return "admin/user-detail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/users";
        }
    }

    @GetMapping("/users/{userId}/edit")
    public String showEditUserForm(@PathVariable Long userId, Model model, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại với ID: " + userId));
            
            UserProfileUpdateDto userProfileUpdateDto = new UserProfileUpdateDto();
            userProfileUpdateDto.setEmail(user.getEmail());
            userProfileUpdateDto.setFullName(user.getFullName());
            userProfileUpdateDto.setPreferredLanguage(user.getPreferredLanguage());
            
            model.addAttribute("user", user);
            model.addAttribute("userProfileUpdateDto", userProfileUpdateDto);
            model.addAttribute("pageTitle", "Chỉnh sửa người dùng");
            return "admin/user-edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/users/{userId}/edit")
    public String updateUserByAdmin(
            @PathVariable Long userId,
            @Valid @ModelAttribute("userProfileUpdateDto") UserProfileUpdateDto userProfileUpdateDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Chỉnh sửa người dùng");
            return "admin/user-edit";
        }
        
        try {
            User updatedUser = userService.updateUserByAdmin(userId, userProfileUpdateDto);
            redirectAttributes.addFlashAttribute("successMessage", "Thông tin người dùng đã được cập nhật thành công.");
            return "redirect:/admin/users/" + userId + "/view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/users/" + userId + "/edit";
        }
    }

    @PostMapping("/users/{userId}/delete")
    public String softDeleteUser(@PathVariable Long userId, RedirectAttributes redirectAttributes, Principal principal) {
        User currentUser = userService.findByUsername(principal.getName()).orElse(null);
        
        if (currentUser != null && currentUser.getId().equals(userId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không thể xóa tài khoản của chính mình.");
            return "redirect:/admin/users";
        }
        
        try {
            userService.softDeleteUser(userId);
            redirectAttributes.addFlashAttribute("successMessage", "Tài khoản người dùng đã được xóa.");
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/users/{userId}/restore")
    public String restoreUser(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            userService.restoreUser(userId);
            redirectAttributes.addFlashAttribute("successMessage", "Tài khoản người dùng đã được khôi phục.");
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/users";
        }
    }

    @GetMapping("/users/search")
    public String searchUsers(
            @ModelAttribute UserSearchDto searchDto,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort,
            Model model) {
        
        String sortField = sort[0];
        String sortDirection = sort.length > 1 ? sort[1] : "asc";
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sortOrder = Sort.by(new Sort.Order(direction, sortField).ignoreCase());
        
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        Page<User> userPage = userService.searchUsers(searchDto, pageable);
        
        model.addAttribute("userPage", userPage);
        model.addAttribute("searchDto", searchDto);
        model.addAttribute("pageTitle", "Tìm kiếm người dùng");
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDirection);
        model.addAttribute("reverseSortDir", sortDirection.equals("asc") ? "desc" : "asc");
        model.addAttribute("roleValues", Role.values());
        
        return "admin/user-search";
    }

    // Quản lý Memory
    @GetMapping("/memories")
    public String listAllMemories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort,
            Model model) {
        
        String sortField = sort[0];
        String sortDirection = sort.length > 1 ? sort[1] : "desc";
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sortOrder = Sort.by(new Sort.Order(direction, sortField).ignoreCase());
        
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        Page<Memory> memoryPage = memoryService.findAllMemories(pageable);
        
        model.addAttribute("memoryPage", memoryPage);
        model.addAttribute("pageTitle", "Quản lý bộ sưu tập ký ức");
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDirection);
        model.addAttribute("reverseSortDir", sortDirection.equals("asc") ? "desc" : "asc");
        
        return "admin/memory-list";
    }

    @GetMapping("/memories/{memoryId}/view")
    public String viewMemoryDetails(@PathVariable Long memoryId, Model model, RedirectAttributes redirectAttributes) {
        try {
            Memory memory = memoryService.findById(memoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Ký ức không tồn tại với ID: " + memoryId));
            
            model.addAttribute("memory", memory);
            model.addAttribute("pageTitle", "Chi tiết ký ức");
            return "admin/memory-detail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/memories";
        }
    }

    @GetMapping("/memories/{memoryId}/edit")
    public String showEditMemoryForm(@PathVariable Long memoryId, Model model, RedirectAttributes redirectAttributes) {
        try {
            Memory memory = memoryService.findById(memoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Ký ức không tồn tại với ID: " + memoryId));
            
            MemoryDto memoryDto = new MemoryDto();
            // Chuyển đổi memory sang memoryDto
            memoryDto.setTitle(memory.getTitle());
            memoryDto.setDescription(memory.getDescription());
            memoryDto.setMemoryDate(memory.getMemoryDate());
            memoryDto.setLocation(memory.getLocation());
            memoryDto.setStatus(memory.getStatus());
            if (memory.getEmotionType() != null) {
                memoryDto.setEmotionTypeId(memory.getEmotionType().getId());
            }
            
            model.addAttribute("memory", memory);
            model.addAttribute("memoryDto", memoryDto);
            model.addAttribute("pageTitle", "Chỉnh sửa ký ức");
            return "admin/memory-edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/memories";
        }
    }

    @PostMapping("/memories/{memoryId}/edit")
    public String updateMemoryByAdmin(
            @PathVariable Long memoryId,
            @Valid @ModelAttribute("memoryDto") MemoryDto memoryDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Chỉnh sửa ký ức");
            return "admin/memory-edit";
        }
        
        try {
            Memory updatedMemory = memoryService.updateMemoryByAdmin(memoryId, memoryDto);
            redirectAttributes.addFlashAttribute("successMessage", "Ký ức đã được cập nhật thành công.");
            return "redirect:/admin/memories/" + memoryId + "/view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/memories/" + memoryId + "/edit";
        }
    }

    @PostMapping("/memories/{memoryId}/delete")
    public String deleteMemoryByAdmin(@PathVariable Long memoryId, RedirectAttributes redirectAttributes) {
        try {
            memoryService.deleteMemoryByAdmin(memoryId);
            redirectAttributes.addFlashAttribute("successMessage", "Ký ức đã được xóa thành công.");
            return "redirect:/admin/memories";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/memories";
        }
    }

    @GetMapping("/memories/search")
    public String searchMemories(
            @ModelAttribute MemorySearchDto searchDto,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort,
            Model model) {
        
        String sortField = sort[0];
        String sortDirection = sort.length > 1 ? sort[1] : "desc";
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sortOrder = Sort.by(new Sort.Order(direction, sortField).ignoreCase());
        
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        Page<Memory> memoryPage = memoryService.searchMemories(searchDto, pageable);
        
        model.addAttribute("memoryPage", memoryPage);
        model.addAttribute("searchDto", searchDto);
        model.addAttribute("pageTitle", "Tìm kiếm ký ức");
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDirection);
        model.addAttribute("reverseSortDir", sortDirection.equals("asc") ? "desc" : "asc");
        
        return "admin/memory-search";
    }

    // Quản lý Log Actions
    @GetMapping("/logs")
    public String viewLogActions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timestamp,desc") String[] sort,
            Model model) {
        
        String sortField = sort[0];
        String sortDirection = sort.length > 1 ? sort[1] : "desc";
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sortOrder = Sort.by(new Sort.Order(direction, sortField).ignoreCase());
        
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        Page<LogAction> logPage = logActionService.findAll(pageable);
        
        model.addAttribute("logPage", logPage);
        model.addAttribute("pageTitle", "Lịch sử hoạt động");
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDirection);
        model.addAttribute("reverseSortDir", sortDirection.equals("asc") ? "desc" : "asc");
        
        return "admin/logs";
    }

    @GetMapping("/logs/search")
    public String searchLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timestamp,desc") String[] sort,
            Model model) {
        
        String sortField = sort[0];
        String sortDirection = sort.length > 1 ? sort[1] : "desc";
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sortOrder = Sort.by(new Sort.Order(direction, sortField).ignoreCase());
        
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        Page<LogAction> logPage = logActionService.findByFilters(username, actionType, startDate, endDate, pageable);
        
        model.addAttribute("logPage", logPage);
        model.addAttribute("username", username);
        model.addAttribute("actionType", actionType);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("pageTitle", "Tìm kiếm lịch sử hoạt động");
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDirection);
        model.addAttribute("reverseSortDir", sortDirection.equals("asc") ? "desc" : "asc");
        
        // Hỗ trợ bộ lọc actionType
        List<String> commonActionTypes = Arrays.asList(
            "LOGIN_SUCCESS", "LOGIN_FAILURE", "LOGOUT", 
            "CREATE_MEMORY", "UPDATE_MEMORY", "DELETE_MEMORY", 
            "VIEW_MEMORY", "CREATE_COMMENT", "DELETE_COMMENT"
        );
        model.addAttribute("commonActionTypes", commonActionTypes);
        
        return "admin/logs-search";
    }
}