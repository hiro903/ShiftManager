package io.shiftmanager.you.mapper;


import io.shiftmanager.you.model.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {
    // 既存のメソッド（メソッド名を統一）
    @Select("SELECT user_id as userId, username, email, password, " +
            "created_at as createdAt, updated_at as updatedAt, " +
            "is_active as active, is_admin as admin " +
            "FROM users WHERE user_id = #{userId}")
    User getUserById(Long userId);

    @Select("SELECT user_id as userId, username, email, password, " +
            "created_at as createdAt, updated_at as updatedAt, " +
            "is_active as active, is_admin as admin " +
            "FROM users")
    List<User> getAllUsers();

    @Insert("INSERT INTO users (username, password, email, is_active, is_admin, created_at, updated_at) " +
            "VALUES (#{username}, #{password}, #{email}, #{active}, #{admin}, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    void insert(User user);

    @Select("SELECT created_at as createdAt, updated_at as updatedAt FROM users WHERE user_id = #{userId}")
    User getTimestamps(Long userId);


    @Update("UPDATE users SET " +
            "username = #{username}, " +
            "email = #{email}, " +
            "is_active = #{active}, " +
            "is_admin = #{admin}, " +
            "updated_at = CURRENT_TIMESTAMP, " +
            "password = CASE WHEN #{password} IS NULL THEN password ELSE #{password} END " +
            "WHERE user_id = #{userId}")
    void update(User user);

    @Delete("DELETE FROM users WHERE user_id = #{userId}")
    void delete(Long userId);

    // Spring Security用の追加メソッド
    @Select("SELECT user_id as userId, username, email, password, " +
            "created_at as createdAt, updated_at as updatedAt, " +
            "is_active as active, is_admin as admin " +
            "FROM users WHERE username = #{username}")
    User findByUsername(String username);

    @Select("SELECT user_id as userId, username, email, password, " +
            "created_at as createdAt, updated_at as updatedAt, " +
            "is_active as active, is_admin as admin " +
            "FROM users WHERE email = #{email}")
    User findByEmail(String email);

    // 追加の便利なメソッド
    @Select("SELECT COUNT(*) FROM users WHERE username = #{username}")
    int countByUsername(String username);

    @Select("SELECT COUNT(*) FROM users WHERE email = #{email}")
    int countByEmail(String email);

    @Update("UPDATE users SET is_active = #{active}, updated_at = CURRENT_TIMESTAMP WHERE user_id = #{userId}")
    void updateActiveStatus(@Param("userId") Long userId, @Param("active") boolean active);
}
