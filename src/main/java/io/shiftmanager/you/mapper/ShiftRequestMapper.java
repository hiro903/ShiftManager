package io.shiftmanager.you.mapper;

import io.shiftmanager.you.model.ShiftRequest;
import io.shiftmanager.you.model.Status;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ShiftRequestMapper {
    
    @Select("SELECT * FROM shift_requests WHERE user_id = #{userId} " +
            "AND YEAR(request_date) = #{year} AND MONTH(request_date) = #{month}")
    List<ShiftRequest> getRequestsByMonth(@Param("userId") Long userId,
                                        @Param("year") int year,
                                        @Param("month") int month);

    @Select("SELECT * FROM shift_requests WHERE user_id = #{userId} " +
            "AND request_date = #{date}")
    List<ShiftRequest> getRequestsByDate(@Param("userId") Long userId,
                                       @Param("date") LocalDate date);

    @Insert("INSERT INTO shift_requests (user_id, request_date, timezone, status, is_submitted) " +
            "VALUES (#{userId}, #{requestDate}, #{timezone}, #{status}, #{isSubmitted})")
    @Options(useGeneratedKeys = true, keyProperty = "requestId")
    void insert(ShiftRequest request);

    @Update("UPDATE shift_requests SET status = #{status}, updated_at = CURRENT_TIMESTAMP " +
            "WHERE request_id = #{requestId}")
    void updateStatus(@Param("requestId") Long requestId, @Param("status") Status status);

    @Update("UPDATE shift_requests SET is_submitted = true, updated_at = CURRENT_TIMESTAMP " +
            "WHERE user_id = #{userId} AND YEAR(request_date) = #{year} AND MONTH(request_date) = #{month}")
    void submitRequests(@Param("userId") Long userId,
                       @Param("year") int year,
                       @Param("month") int month);

    @Delete("DELETE FROM shift_requests WHERE request_id = #{requestId}")
    void delete(Long requestId);

    @Select("SELECT * FROM shift_requests WHERE request_id = #{requestId}")
    ShiftRequest getRequestById(Long requestId);
} 