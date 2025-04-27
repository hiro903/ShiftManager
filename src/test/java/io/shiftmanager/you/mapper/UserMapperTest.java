package io.shiftmanager.you.mapper;

import io.shiftmanager.you.config.TestConfig;
import io.shiftmanager.you.model.User;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * UserMapperを使用したデータベース操作のテストクラス
 */
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(TestConfig.class)
public class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setup() {
        cleanupTestData();
    }

    private void cleanupTestData() {
        try {
            List<User> users = userMapper.getAllUsers();
            for (User user : users) {
                userMapper.delete(user.getUserId());
            }
        } catch (Exception e) {
            System.err.println("Failed to cleanup test data: " + e.getMessage());
        }
    }

    private User createTestUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("testPassword"));
        user.setEmail(email);
        user.setActive(true);
        user.setAdmin(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    // ユーザーの新規作成と取得の基本機能をテスト
    @Test
    @Transactional
    public void testInsertAndGetUser() {
        User user = createTestUser("testUser", "test@example.com");
        userMapper.insert(user);
        User retrievedUser = userMapper.getUserById(user.getUserId());

        assertThat(retrievedUser).isNotNull();
        assertThat(retrievedUser.getUsername()).isEqualTo("testUser");
        assertThat(retrievedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(passwordEncoder.matches("testPassword", retrievedUser.getPassword())).isTrue();
    }

    // メールアドレスの重複チェックをテスト
    @Test
    @Transactional
    public void testDuplicateEmail() {
        User user1 = createTestUser("user1", "same@example.com");
        User user2 = createTestUser("user2", "same@example.com");
        userMapper.insert(user1);

        assertThatThrownBy(() -> userMapper.insert(user2))
                .isInstanceOf(DuplicateKeyException.class);
    }

    // 無効なデータでのユーザー作成時のエラー処理をテスト
    @Test
    @Transactional
    public void testInvalidData() {
        User invalidUser = new User();
        assertThatThrownBy(() -> userMapper.insert(invalidUser))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // 全ユーザーの取得機能をテスト
    @Test
    @Transactional
    public void testGetAllUsers() {
        User user1 = createTestUser("user1", "user1@example.com");
        User user2 = createTestUser("user2", "user2@example.com");
        user2.setAdmin(true);

        userMapper.insert(user1);
        userMapper.insert(user2);

        List<User> users = userMapper.getAllUsers();

        assertThat(users).isNotEmpty();
        assertThat(users).hasSizeGreaterThanOrEqualTo(2);

        User foundUser1 = users.stream()
                .filter(u -> u.getEmail().equals("user1@example.com"))
                .findFirst()
                .orElse(null);
        User foundUser2 = users.stream()
                .filter(u -> u.getEmail().equals("user2@example.com"))
                .findFirst()
                .orElse(null);

        assertThat(foundUser1).isNotNull();
        assertThat(foundUser2).isNotNull();
        assertThat(foundUser2.isAdmin()).isTrue();
    }

    // ユーザー情報の更新機能をテスト
    @Test
    @Transactional
    public void testUpdateUser() {
        User user = createTestUser("originalUser", "original@example.com");
        userMapper.insert(user);

        user.setUsername("updatedUser");
        user.setEmail("updated@example.com");
        userMapper.update(user);

        User updatedUser = userMapper.getUserById(user.getUserId());
        assertThat(updatedUser.getUsername()).isEqualTo("updatedUser");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
    }

    // ユーザーの削除機能をテスト
    @Test
    @Transactional
    public void testDeleteUser() {
        User user = createTestUser("deleteUser", "delete@example.com");
        userMapper.insert(user);

        userMapper.delete(user.getUserId());

        User deletedUser = userMapper.getUserById(user.getUserId());
        assertThat(deletedUser).isNull();
    }
}
