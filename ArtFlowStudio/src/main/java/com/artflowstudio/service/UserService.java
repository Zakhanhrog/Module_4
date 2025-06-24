package com.artflowstudio.service;

import com.artflowstudio.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User saveUser(User user); // Dùng cho cả tạo mới và cập nhật cơ bản
    Optional<User> findByUsername(String username);
    Optional<User> findById(Long id);
    List<User> findAllUsers(); // Lấy tất cả user
    List<User> findAllLearners(); // Lấy user có vai trò LEARNER
    User updateUserStatus(Long userId, boolean enabled);
    void changePassword(User user, String newPassword);
}