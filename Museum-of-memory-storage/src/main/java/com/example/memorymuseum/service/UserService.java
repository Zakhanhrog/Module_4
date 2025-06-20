package com.example.memorymuseum.service;

import com.example.memorymuseum.dto.UserRegistrationDto; // Giữ lại nếu cần
import com.example.memorymuseum.dto.UserProfileUpdateDto; // DTO mới
import com.example.memorymuseum.dto.UserPasswordChangeDto; // DTO mới
import com.example.memorymuseum.dto.UserSearchDto; // DTO mới để tìm kiếm
import com.example.memorymuseum.model.Role;
import com.example.memorymuseum.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface UserService {
    User registerNewUserAccount(UserRegistrationDto registrationDto);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> getCurrentUser();
    User save(User user); // Có thể giữ lại cho các mục đích khác
    Optional<User> findById(Long userId);
    // Phương thức mới
    User updateProfile(String currentUsername, UserProfileUpdateDto profileUpdateDto);
    void changePassword(String currentUsername, UserPasswordChangeDto passwordChangeDto);
    String updateUserAvatar(String currentUsername, MultipartFile avatarFile);
    Page<User> findAllUsers(Pageable pageable);
    long countTotalUsers();
    User changeUserRole(Long userId, Role newRole);
    User toggleUserEnabledStatus(Long userId);
    
    // Các phương thức mới cho chức năng quản lý user (admin)
    User updateUserByAdmin(Long userId, UserProfileUpdateDto profileUpdateDto);
    void softDeleteUser(Long userId);
    void restoreUser(Long userId);
    Page<User> findActiveUsers(Pageable pageable);
    Page<User> findDeletedUsers(Pageable pageable);
    Page<User> searchUsers(UserSearchDto searchDto, Pageable pageable);
}