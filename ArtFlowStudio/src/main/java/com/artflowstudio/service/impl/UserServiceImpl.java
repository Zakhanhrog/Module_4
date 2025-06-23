package com.artflowstudio.service.impl;

import com.artflowstudio.entity.User;
import com.artflowstudio.repository.UserRepository;
import com.artflowstudio.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // Sẽ inject sau
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Sẽ inject sau khi có SecurityConfig

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User saveUser(User user) {
        // Mật khẩu nên được mã hóa trước khi gọi hàm này
        // Hoặc bạn có thể thêm logic mã hóa ở đây nếu user.password chưa được mã hóa
        // Ví dụ: if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
        //          user.setPassword(passwordEncoder.encode(user.getPassword()));
        //       }
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
}