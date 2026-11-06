package com.example.shopping.config;

import com.example.shopping.entity.User;
import com.example.shopping.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // 初始化管理员账号
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@example.com")
                    .phone("13800138000")
                    .enabled(true)
                    .isAdmin(true)
                    .build();
            userRepository.save(admin);
            System.out.println("✅ 管理员账号初始化成功: admin / admin123");
        } else {
            // 如果已存在，检查密码是否是BCrypt格式，如果不是则更新
            User existingAdmin = userRepository.findByUsername("admin").orElse(null);
            if (existingAdmin != null && !existingAdmin.getPassword().startsWith("$2a$")) {
                existingAdmin.setPassword(passwordEncoder.encode("admin123"));
                userRepository.save(existingAdmin);
                System.out.println("✅ 管理员密码已更新为BCrypt格式");
            }
        }
    }
}
