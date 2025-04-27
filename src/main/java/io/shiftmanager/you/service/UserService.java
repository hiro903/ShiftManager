package io.shiftmanager.you.service;

import io.shiftmanager.you.exception.UserNotFoundException;
import io.shiftmanager.you.exception.DuplicateEmailException;
import io.shiftmanager.you.model.User;
import io.shiftmanager.you.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public User getUserById(Long id) {
        User user = userMapper.getUserById(id);
        if (user == null) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        return user;
    }

    public List<User> getAllUsers() {
        return userMapper.getAllUsers();
    }

    @Transactional
    public User createUser(User user) {
        // メールアドレスの重複チェック
        if (existsByEmail(user.getEmail())) {
            throw new DuplicateEmailException("このメールアドレスは既に使用されています");
        }

        // パスワードをハッシュ化
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // デフォルト値を設定
        user.setActive(true);
        userMapper.insert(user);

        // タイムスタンプを取得して設定
        User timestamps = userMapper.getTimestamps(user.getUserId());
        user.setCreatedAt(timestamps.getCreatedAt());
        user.setUpdatedAt(timestamps.getUpdatedAt());

        return user;
    }

    @Transactional
    public User updateUser(User user) {
        if (userMapper.getUserById(user.getUserId()) == null) {
            throw new UserNotFoundException("User not found with id: " + user.getUserId());
        }
        // パスワードが変更される場合のみハッシュ化
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userMapper.update(user);

        // タイムスタンプを取得して設定
        User timestamps = userMapper.getTimestamps(user.getUserId());
        user.setCreatedAt(timestamps.getCreatedAt());
        user.setUpdatedAt(timestamps.getUpdatedAt());

        return user;
    }

    @Transactional
    public void deleteUser(Long id) {
        if (userMapper.getUserById(id) == null) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        userMapper.delete(id);
    }

    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    public User findByEmail(String email) {
        User user = userMapper.findByEmail(email);
        if (user != null) {
            // 管理者権限のデバッグログを出力
            if (user.isAdmin()) {
                System.out.println("管理者ユーザーが見つかりました: " + email);
            } else {
                System.out.println("一般ユーザーが見つかりました: " + email);
            }
        }
        return user;
    }

    public boolean existsByUsername(String username) {
        return userMapper.countByUsername(username) > 0;
    }

    public boolean existsByEmail(String email) {
        return userMapper.countByEmail(email) > 0;
    }

    @Transactional
    public void updateActiveStatus(Long userId, boolean isActive) {
        if (userMapper.getUserById(userId) == null) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }
        userMapper.updateActiveStatus(userId, isActive);
    }
}