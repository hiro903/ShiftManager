package io.shiftmanager.you.mapper;

import io.shiftmanager.you.model.Shift;
import io.shiftmanager.you.model.Status;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.EnumTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ShiftMapper {
    
    @Select("SELECT * FROM shifts WHERE user_id = #{userId} " +
            "AND YEAR(shift_date) = #{year} AND MONTH(shift_date) = #{month}")
    List<Shift> getShiftsByMonth(@Param("userId") Long userId,
                                @Param("year") int year,
                                @Param("month") int month);

    @Select("SELECT * FROM shifts WHERE user_id = #{userId} AND shift_date = #{date}")
    List<Shift> getShiftsByDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Insert("INSERT INTO shifts (user_id, shift_date, timezone, status) " +
            "VALUES (#{userId}, #{shiftDate}, #{timezone}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "shiftId")
    void insert(Shift shift);

    @Update("UPDATE shifts SET status = #{status}, updated_at = CURRENT_TIMESTAMP " +
            "WHERE shift_id = #{shiftId}")
    void updateStatus(@Param("shiftId") Long shiftId, @Param("status") Status status);
    
    @Update("UPDATE shifts SET timezone = #{timezone}, status = #{status}, updated_at = CURRENT_TIMESTAMP " +
            "WHERE shift_id = #{shiftId}")
    void update(Shift shift);

    @Delete("DELETE FROM shifts WHERE shift_id = #{shiftId}")
    void delete(Long shiftId);

    @Select("SELECT * FROM shifts WHERE shift_id = #{shiftId}")
    Shift getShiftById(Long shiftId);
} 