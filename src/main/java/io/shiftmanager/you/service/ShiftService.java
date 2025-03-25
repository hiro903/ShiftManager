package io.shiftmanager.you.service;

import io.shiftmanager.you.mapper.ShiftMapper;
import io.shiftmanager.you.model.Shift;
import io.shiftmanager.you.model.Status;
import io.shiftmanager.you.model.Timezone;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ShiftService {

    private final ShiftMapper shiftMapper;

    public List<Shift> getShiftsByMonth(Long userId, int year, int month) {
        return shiftMapper.getShiftsByMonth(userId, year, month);
    }

    @Transactional
    public void requestShift(Long userId, LocalDate date, String timezoneStr) {
        // すでに存在する場合は削除して再登録
        List<Shift> existingShifts = shiftMapper.getShiftsByDate(userId, date);
        for (Shift existing : existingShifts) {
            if (Status.REQUESTED.equals(existing.getStatus())) {
                shiftMapper.delete(existing.getShiftId());
            }
        }
        
        // NONEの場合は登録しない（削除のみ）
        if ("NONE".equals(timezoneStr)) {
            return;
        }
        
        Shift shift = new Shift();
        shift.setUserId(userId);
        shift.setShiftDate(date);
        shift.setTimezone(Timezone.fromString(timezoneStr));
        shift.setStatus(Status.REQUESTED);
        shiftMapper.insert(shift);
    }

    @Transactional
    public void cancelShift(Long shiftId) {
        Shift shift = shiftMapper.getShiftById(shiftId);
        if (shift != null && Status.REQUESTED.equals(shift.getStatus())) {
            shiftMapper.delete(shiftId);
        }
    }
    
    /**
     * 指定された月の指定曜日に一括でシフトを登録
     */
    @Transactional
    public void bulkRequestShiftByDayOfWeek(Long userId, int year, int month, Set<Integer> daysOfWeek, String timezoneStr) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            int dayOfWeekValue = date.getDayOfWeek().getValue() % 7; // 0（日）〜6（土）
            if (daysOfWeek.contains(dayOfWeekValue)) {
                requestShift(userId, date, timezoneStr);
            }
            date = date.plusDays(1);
        }
    }
    
    /**
     * 指定された月のすべてのシフトをリセット
     */
    @Transactional
    public void resetShiftsForMonth(Long userId, int year, int month) {
        List<Shift> shifts = getShiftsByMonth(userId, year, month);
        for (Shift shift : shifts) {
            if (Status.REQUESTED.equals(shift.getStatus())) {
                shiftMapper.delete(shift.getShiftId());
            }
        }
    }
    
    /**
     * シフトの一括保存（ステータスを変更せず保存のみ）
     */
    @Transactional
    public void saveShifts(Long userId, List<Shift> shifts) {
        // 現在のシフトはそのまま維持（フロントエンドで完全な状態が送られてくる前提）
        for (Shift shift : shifts) {
            if (shift.getUserId().equals(userId)) {
                // 既存シフトを確認
                Shift existing = shiftMapper.getShiftById(shift.getShiftId());
                if (existing != null) {
                    // 更新
                    shiftMapper.update(shift);
                } else {
                    // 新規作成
                    shift.setStatus(Status.REQUESTED);
                    shiftMapper.insert(shift);
                }
            }
        }
    }
    
    /**
     * シフトの提出（ステータスをAPPROVEDに変更）
     */
    @Transactional
    public void submitShifts(Long userId, int year, int month) {
        List<Shift> shifts = getShiftsByMonth(userId, year, month);
        for (Shift shift : shifts) {
            if (Status.REQUESTED.equals(shift.getStatus())) {
                shift.setStatus(Status.APPROVED);
                shiftMapper.update(shift);
            }
        }
    }
} 