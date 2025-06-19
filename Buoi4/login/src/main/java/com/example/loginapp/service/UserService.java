package com.example.loginapp.service;

import com.example.loginapp.model.Login;
import com.example.loginapp.model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service // Marks this as a Spring-managed service
public class UserService {

    private static final List<User> users;

    static {
        users = new ArrayList<>();
        users.add(new User("user1", "pass1", "John Doe", "john.doe@example.com", 30));
        users.add(new User("admin", "admin123", "Administrator", "admin@example.com", 45));
        users.add(new User("testuser", "test", "Test User", "test@example.com", 25));
    }

    public User checkLogin(Login login) {
        for (User user : users) {
            if (user.getAccount().equals(login.getAccount()) && user.getPassword().equals(login.getPassword())) {
                return user; // Return the full User object
            }
        }
        return null; // Login failed
    }
}