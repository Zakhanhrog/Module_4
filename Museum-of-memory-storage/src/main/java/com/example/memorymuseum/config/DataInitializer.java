package com.example.memorymuseum.config;

import com.example.memorymuseum.model.Role;
import com.example.memorymuseum.model.User;
import com.example.memorymuseum.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🔧 DataInitializer đang chạy...");
        
        // Kiểm tra và tạo/reset admin user
        boolean adminExists = userRepository.existsByUsername("admin");
        System.out.println("🔍 Kiểm tra admin tồn tại: " + adminExists);
        
        User admin;
        if (adminExists) {
            // Reset lại admin user nếu đã tồn tại
            admin = userRepository.findByUsername("admin").orElse(null);
            System.out.println("🔄 Reset lại tài khoản admin...");
        } else {
            // Tạo mới admin user
            admin = new User();
            System.out.println("✨ Tạo mới tài khoản admin...");
        }
        
        if (admin != null) {
            admin.setUsername("admin");
            admin.setEmail("admin@memorymuseum.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("Administrator");
            admin.setRole(Role.ADMIN);
            admin.setPreferredLanguage("vi");
            admin.setEnabled(true);
            
            userRepository.save(admin);
            System.out.println("✓ Tài khoản admin đã được cập nhật:");
            System.out.println("  Username: admin");
            System.out.println("  Password: admin123");
            System.out.println("  Email: admin@memorymuseum.com");
            System.out.println("  Role: " + admin.getRole());
            System.out.println("  Enabled: " + admin.isEnabled());
        }
    }
}
