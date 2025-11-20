package com.ims.stockmanagement.config;

import com.ims.stockmanagement.enums.UserRole;
import com.ims.stockmanagement.models.User;
import com.ims.stockmanagement.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner init(UserRepository userRepository) {
        return args -> {
            // Eğer hiç kullanıcı yoksa yeni admin oluştur
            if (userRepository.count() == 0) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@local");
                admin.setFullName("System Admin");
                admin.setPassword(new BCryptPasswordEncoder().encode("admin123"));
                admin.setRole(UserRole.ADMIN);
                admin.setEnabled(true);
                userRepository.save(admin);
                System.out.println("Created default admin: admin/admin123");
                return;
            }

            // Eğer ADMIN rolünde kullanıcı yoksa ilk kullanıcıyı admin yap
            List<User> users = userRepository.findAll();
            boolean hasAdmin = users.stream().anyMatch(u -> u.getRole() == UserRole.ADMIN);
            if (!hasAdmin) {
                User first = users.get(0);
                first.setRole(UserRole.ADMIN);
                userRepository.save(first);
                System.out.println("Promoted existing user to ADMIN: " + first.getUsername());
            }
        };
    }
}
