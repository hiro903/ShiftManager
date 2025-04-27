package io.shiftmanager.you.mapper;

import io.shiftmanager.you.model.Shift;
import io.shiftmanager.you.model.User;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ShiftMapper {

    @Select("SELECT s.shift_id as shiftId, s.user_id as userId, s.shift_date as shiftDate, " +
            "s.timezone, s.status, s.created_at as createdAt, s.updated_at as updatedAt, " +
            "u.username " +
            "FROM shifts s " +
            "LEFT JOIN users u ON s.user_id = u.user_id " +
            "WHERE s.user_id = #{userId} AND s.shift_date BETWEEN #{startDate} AND #{endDate}")
    List<Shift> findByUserIdAndShiftDateBetween(@Param("userId") Long userId, 
                                              @Param("startDate") LocalDate startDate, 
                                              @Param("endDate") LocalDate endDate);

    @Select("SELECT s.shift_id as shiftId, s.user_id as userId, s.shift_date as shiftDate, " +
            "s.timezone, s.status, s.created_at as createdAt, s.updated_at as updatedAt, " +
            "u.username " +
            "FROM shifts s " +
            "LEFT JOIN users u ON s.user_id = u.user_id " +
            "WHERE s.shift_date BETWEEN #{startDate} AND #{endDate}")
    List<Shift> findByShiftDateBetween(@Param("startDate") LocalDate startDate, 
                                     @Param("endDate") LocalDate endDate);

    @Delete("DELETE FROM shifts WHERE user_id = #{userId} AND shift_date BETWEEN #{startDate} AND #{endDate}")
    void deleteByUserIdAndShiftDateBetween(@Param("userId") Long userId, 
                                         @Param("startDate") LocalDate startDate, 
                                         @Param("endDate") LocalDate endDate);

    @Select("SELECT shift_id as shiftId, user_id as userId, shift_date as shiftDate, " +
            "timezone, status, created_at as createdAt, updated_at as updatedAt " +
            "FROM shifts " +
            "WHERE user_id = #{userId} AND shift_date = #{date} AND timezone = #{timezone}")
    Shift findByUserIdAndShiftDateAndTimezone(@Param("userId") Long userId, 
                                            @Param("date") LocalDate date, 
                                            @Param("timezone") String timezone);

    @Select("SELECT u.user_id as userId, u.username, u.email, u.password, " +
            "u.is_active as active, u.is_admin as admin, " +
            "u.created_at as createdAt, u.updated_at as updatedAt " +
            "FROM users u " +
            "JOIN shifts s ON u.user_id = s.user_id " +
            "WHERE s.shift_date = #{date} AND s.timezone = #{timezone} AND s.status = 'SUBMITTED'")
    List<User> findAvailableStaffByDateAndTimezone(@Param("date") LocalDate date, 
                                                 @Param("timezone") String timezone);
    
    @Insert("INSERT INTO shifts (user_id, shift_date, timezone, status, created_at, updated_at) " +
            "VALUES (#{userId}, #{shiftDate}, #{timezone}, #{status}, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")
    @Options(useGeneratedKeys = true, keyProperty = "shiftId")
    void insert(Shift shift);
    
    @Update("UPDATE shifts SET status = #{status}, updated_at = CURRENT_TIMESTAMP " +
            "WHERE shift_id = #{shiftId}")
    void updateStatus(@Param("shiftId") Long shiftId, @Param("status") String status);
    
    @Update("UPDATE shifts SET status = #{status}, updated_at = CURRENT_TIMESTAMP " +
            "WHERE user_id = #{userId} AND shift_date BETWEEN #{startDate} AND #{endDate}")
    void updateStatusByUserIdAndDateRange(@Param("userId") Long userId, 
                                       @Param("startDate") LocalDate startDate, 
                                       @Param("endDate") LocalDate endDate, 
                                       @Param("status") String status);
    
    @Delete("DELETE FROM shifts WHERE shift_id = #{shiftId}")
    void deleteById(Long shiftId);
    
    @Select("SELECT shift_id as shiftId, user_id as userId, shift_date as shiftDate, " +
            "timezone, status, created_at as createdAt, updated_at as updatedAt " +
            "FROM shifts WHERE shift_id = #{shiftId}")
    Shift findById(Long shiftId);
    
    @Insert("<script>" +
            "INSERT INTO shifts (user_id, shift_date, timezone, status, created_at, updated_at) VALUES " +
            "<foreach collection='shifts' item='shift' separator=','>" +
            "(#{shift.userId}, #{shift.shiftDate}, #{shift.timezone}, #{shift.status}, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)" +
            "</foreach>" +
            "</script>")
    void batchInsert(@Param("shifts") List<Shift> shifts);
} 