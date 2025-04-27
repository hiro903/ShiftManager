package io.shiftmanager.you.mapper;

import io.shiftmanager.you.model.ConfirmedShift;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ConfirmedShiftMapper {
    
    @Select("SELECT * FROM confirmed_shifts WHERE user_id = #{userId} " +
            "AND YEAR(confirmed_date) = #{year} AND MONTH(confirmed_date) = #{month}")
    List<ConfirmedShift> getConfirmedShiftsByMonth(@Param("userId") Long userId,
                                                  @Param("year") int year,
                                                  @Param("month") int month);

    @Insert("INSERT INTO confirmed_shifts (user_id, request_id, confirmed_date, timezone) " +
            "VALUES (#{userId}, #{requestId}, #{confirmedDate}, #{timezone})")
    @Options(useGeneratedKeys = true, keyProperty = "confirmedId")
    void insert(ConfirmedShift confirmedShift);

    @Delete("DELETE FROM confirmed_shifts WHERE confirmed_id = #{confirmedId}")
    void delete(Long confirmedId);

    @Select("SELECT * FROM confirmed_shifts WHERE confirmed_id = #{confirmedId}")
    ConfirmedShift getConfirmedShiftById(Long confirmedId);

    @Select("SELECT * FROM confirmed_shifts WHERE request_id = #{requestId}")
    ConfirmedShift getConfirmedShiftByRequestId(Long requestId);
} 