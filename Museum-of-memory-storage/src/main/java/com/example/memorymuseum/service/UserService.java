package com.example.memorymuseum.service;

import com.example.memorymuseum.dto.UserRegistrationDto;
import com.example.memorymuseum.model.User;

import java.util.Optional;

public interface UserService {
    User registerNewUserAccount(UserRegistrationDto registrationDto);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> getCurrentUser();
    User save(User user);
    void updateUserProfile(User user, String fullName, String email);
    void changePassword(User user, String newPassword);
}