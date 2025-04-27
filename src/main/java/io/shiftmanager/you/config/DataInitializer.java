package io.shiftmanager.you.config;

import io.shiftmanager.you.mapper.UserMapper;
import io.shiftmanager.you.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        initializeAdminUser();
        initializeTestUser();
    }

    private void initializeAdminUser() {
        try {
            String adminEmail = "admin@symphony.com";
            User existingAdmin = userMapper.findByEmail(adminEmail);
            
            if (existingAdmin != null) {
                // パスワードが正しくハッシュ化されているか確認
                if (!isPasswordHashed(existingAdmin.getPassword())) {
                    log.info("管理者ユーザーのパスワードを更新します...");
                    existingAdmin.setPassword(passwordEncoder.encode("admin123"));
                    userMapper.update(existingAdmin);
                    log.info("管理者ユーザーのパスワードを更新しました");
                }
            } else {
                log.info("管理者ユーザーを作成します...");
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setActive(true);
                admin.setAdmin(true);
                userMapper.insert(admin);
                log.info("管理者ユーザーを作成しました");
            }
        } catch (Exception e) {
            log.error("管理者ユーザーの初期化中にエラーが発生しました: {}", e.getMessage());
        }
    }

    private void initializeTestUser() {
        try {
            String userEmail = "user@example.com";
            User existingUser = userMapper.findByEmail(userEmail);
            
            if (existingUser != null) {
                // パスワードが正しくハッシュ化されているか確認
                if (!isPasswordHashed(existingUser.getPassword())) {
                    log.info("テストユーザーのパスワードを更新します...");
                    existingUser.setPassword(passwordEncoder.encode("user123"));
                    userMapper.update(existingUser);
                    log.info("テストユーザーのパスワードを更新しました");
                }
            } else {
                log.info("テストユーザーを作成します...");
                User user = new User();
                user.setUsername("user");
                user.setEmail(userEmail);
                user.setPassword(passwordEncoder.encode("user123"));
                user.setActive(true);
                user.setAdmin(false);
                userMapper.insert(user);
                log.info("テストユーザーを作成しました");
            }
        } catch (Exception e) {
            log.error("テストユーザーの初期化中にエラーが発生しました: {}", e.getMessage());
        }
    }

    private boolean isPasswordHashed(String password) {
        return password != null && password.startsWith("$2a$");
    }
} 