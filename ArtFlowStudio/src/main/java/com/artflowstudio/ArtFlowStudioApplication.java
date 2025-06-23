package com.artflowstudio;

import com.artflowstudio.entity.User;
import com.artflowstudio.enums.Role;
import com.artflowstudio.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class ArtFlowStudioApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArtFlowStudioApplication.class, args);
    }

    @Bean
    @DependsOn("entityManagerFactory")
    CommandLineRunner run(UserService userService, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userService.findByUsername("admin@artflow.com").isEmpty()) {
                User adminUser = new User();
                adminUser.setUsername("admin@artflow.com");
                adminUser.setPassword(passwordEncoder.encode("admin123"));
                adminUser.setFullName("Administrator");
                adminUser.setEnabled(true);

                Set<Role> adminRoles = new HashSet<>();
                adminRoles.add(Role.ADMIN);
                adminUser.setRoles(adminRoles);

                userService.saveUser(adminUser);
                System.out.println("Admin user created: admin@artflow.com / admin123");
            } else {
                System.out.println("Admin user 'admin@artflow.com' already exists.");
            }
        };
    }
}