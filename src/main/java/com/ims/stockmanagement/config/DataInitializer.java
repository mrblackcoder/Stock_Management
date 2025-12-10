package com.ims.stockmanagement.config;

import com.ims.stockmanagement.enums.UserRole;
import com.ims.stockmanagement.models.User;
import com.ims.stockmanagement.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

@Slf4j
@Configuration
public class DataInitializer {

    @Value("${admin.default.password:#{null}}")
    private String adminPassword;

    @Bean
    CommandLineRunner init(UserRepository userRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                if (adminPassword == null || adminPassword.isEmpty()) {
                    log.warn("ADMIN_PASSWORD not set. Using default password. Please set ADMIN_PASSWORD environment variable in production!");
                    adminPassword = "Admin@123!Secure";
                }

                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@local");
                admin.setFullName("System Admin");
                admin.setPassword(new BCryptPasswordEncoder().encode(adminPassword));
                admin.setRole(UserRole.ADMIN);
                admin.setEnabled(true);
                userRepository.save(admin);
                log.info("Default admin user initialized. Please change the password after first login.");
                return;
            }

            // If no ADMIN role exists, promote the first user
            List<User> users = userRepository.findAll();
            boolean hasAdmin = users.stream().anyMatch(u -> u.getRole() == UserRole.ADMIN);
            if (!hasAdmin && !users.isEmpty()) {
                User first = users.get(0);
                first.setRole(UserRole.ADMIN);
                userRepository.save(first);
                log.info("Existing user promoted to ADMIN role");
            }
        };
    }
}
