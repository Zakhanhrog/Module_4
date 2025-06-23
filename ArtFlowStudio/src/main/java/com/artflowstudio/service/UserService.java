package com.artflowstudio.service;

import com.artflowstudio.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User saveUser(User user);
    Optional<User> findByUsername(String username);
    Optional<User> findById(Long id);
    List<User> findAllUsers();
}