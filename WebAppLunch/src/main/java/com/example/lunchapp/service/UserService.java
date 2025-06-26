package com.example.lunchapp.service;

import com.example.lunchapp.model.dto.UserRegistrationDto;
import com.example.lunchapp.model.entity.User;
import java.math.BigDecimal;
import java.util.List; // Thêm import
import java.util.Optional;

public interface UserService {
    User registerUser(UserRegistrationDto registrationDto);
    Optional<User> findByUsername(String username);
    User save(User user);
    Optional<User> findById(Long id);
    void changePassword(Long userId, String newPassword);
    User depositMoney(Long userId, BigDecimal amount);

    // --- Methods for Admin ---
    List<User> getAllUsers();
    User updateUserByAdmin(Long userId, User updatedUserPartialInfo, String newPassword, List<Long> roleIds); // Thêm roleIds
    void toggleUserStatus(Long userId);
}