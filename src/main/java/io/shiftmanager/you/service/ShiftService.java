package io.shiftmanager.you.service;

import io.shiftmanager.you.mapper.ShiftMapper;
import io.shiftmanager.you.model.Shift;
import io.shiftmanager.you.model.Status;
import io.shiftmanager.you.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ShiftService {

    private final ShiftMapper shiftMapper;

    public List<Shift> getShiftsByMonth(Long userId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = YearMonth.of(year, month).atEndOfMonth();
        return shiftMapper.findByUserIdAndShiftDateBetween(userId, startDate, endDate);
    }

    public List<Shift> getAllShiftsByMonth(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        return shiftMapper.findByShiftDateBetween(startDate, endDate);
    }

    @Transactional
    public void requestShift(Long userId, LocalDate date, String timezone) {
        Shift shift = new Shift();
        shift.setUserId(userId);
        shift.setShiftDate(date);
        shift.setTimezone(timezone);
        
        requestShift(shift);
    }
    
    @Transactional
    public void requestShift(Shift shift) {
        Shift existingShift = shiftMapper.findByUserIdAndShiftDateAndTimezone(
                shift.getUserId(), shift.getShiftDate(), shift.getTimezone());

        if (existingShift == null) {
            shift.setStatus(Status.REQUESTED);
            shiftMapper.insert(shift);
        } else {
            existingShift.setStatus(Status.REQUESTED);
            shiftMapper.updateStatus(existingShift.getShiftId(), Status.REQUESTED.name());
        }
    }

    @Transactional
    public void cancelShift(Long shiftId) {
        shiftMapper.deleteById(shiftId);
    }
    
    @Transactional
    public void cancelShift(Long userId, Long shiftId) {
        // ユーザーIDを使用した追加の検証が必要な場合はここに実装
        cancelShift(shiftId);
    }

    @Transactional
    public void bulkRequestShiftByDayOfWeek(Long userId, int year, int month, Set<Integer> daysOfWeekValues, String timezone) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Shift> shifts = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            if (daysOfWeekValues.contains(currentDate.getDayOfWeek().getValue())) {
                Shift shift = new Shift();
                shift.setUserId(userId);
                shift.setShiftDate(currentDate);
                shift.setTimezone(timezone);
                shift.setStatus(Status.REQUESTED);
                shifts.add(shift);
            }
            currentDate = currentDate.plusDays(1);
        }
        
        if (!shifts.isEmpty()) {
            shiftMapper.batchInsert(shifts);
        }
    }

    @Transactional
    public void resetShiftsForMonth(Long userId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = YearMonth.of(year, month).atEndOfMonth();
        shiftMapper.deleteByUserIdAndShiftDateBetween(userId, startDate, endDate);
    }

    @Transactional
    public void submitShifts(Long userId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = YearMonth.of(year, month).atEndOfMonth();
        shiftMapper.updateStatusByUserIdAndDateRange(userId, startDate, endDate, Status.SUBMITTED.name());
    }

    public List<Shift> getShiftsByDateAndTimezone(LocalDate date, String timezone) {
        LocalDate startDate = date;
        LocalDate endDate = date;
        List<Shift> shifts = shiftMapper.findByShiftDateBetween(startDate, endDate);
        
        List<Shift> filteredShifts = new ArrayList<>();
        for (Shift shift : shifts) {
            if (timezone.equals(shift.getTimezone())) {
                filteredShifts.add(shift);
            }
        }
        
        return filteredShifts;
    }

    @Transactional
    public void confirmShifts(List<Long> shiftIds) {
        if (shiftIds == null || shiftIds.isEmpty()) {
            return;
        }
        
        for (Long shiftId : shiftIds) {
            Shift shift = shiftMapper.findById(shiftId);
            if (shift != null && Status.SUBMITTED.equals(shift.getStatus())) {
                shiftMapper.updateStatus(shiftId, Status.CONFIRMED.name());
            }
        }
    }

    @Transactional
    public void publishShifts(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = YearMonth.of(year, month).atEndOfMonth();
        List<Shift> shifts = shiftMapper.findByShiftDateBetween(startDate, endDate);
        
        for (Shift shift : shifts) {
            if (Status.CONFIRMED.equals(shift.getStatus())) {
                shiftMapper.updateStatus(shift.getShiftId(), Status.PUBLISHED.name());
            }
        }
    }

    public List<Shift> getPublishedShiftsByUser(Long userId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = YearMonth.of(year, month).atEndOfMonth();
        List<Shift> allShifts = shiftMapper.findByUserIdAndShiftDateBetween(userId, startDate, endDate);
        
        List<Shift> publishedShifts = new ArrayList<>();
        for (Shift shift : allShifts) {
            if (Status.PUBLISHED.equals(shift.getStatus())) {
                publishedShifts.add(shift);
            }
        }
        
        return publishedShifts;
    }

    public List<Shift> getAllPublishedShifts(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = YearMonth.of(year, month).atEndOfMonth();
        List<Shift> allShifts = shiftMapper.findByShiftDateBetween(startDate, endDate);
        
        List<Shift> publishedShifts = new ArrayList<>();
        for (Shift shift : allShifts) {
            if (Status.PUBLISHED.equals(shift.getStatus())) {
                publishedShifts.add(shift);
            }
        }
        
        return publishedShifts;
    }

    // 管理者用メソッド
    public List<User> getAvailableStaff(LocalDate date, String timezone) {
        return shiftMapper.findAvailableStaffByDateAndTimezone(date, timezone);
    }

    @Transactional
    public void assignShift(Long userId, LocalDate date, String timezone) {
        Shift shift = shiftMapper.findByUserIdAndShiftDateAndTimezone(userId, date, timezone);
        if (shift == null) {
            throw new RuntimeException("シフトが見つかりません");
        }
        shiftMapper.updateStatus(shift.getShiftId(), Status.CONFIRMED.name());
    }

    @Transactional
    public void unassignShift(Long userId, LocalDate date, String timezone) {
        Shift shift = shiftMapper.findByUserIdAndShiftDateAndTimezone(userId, date, timezone);
        if (shift == null) {
            throw new RuntimeException("シフトが見つかりません");
        }
        shiftMapper.updateStatus(shift.getShiftId(), Status.SUBMITTED.name());
    }

    @Transactional
    public void saveAdminShifts(List<Shift> shifts) {
        for (Shift shift : shifts) {
            Shift existingShift = shiftMapper.findByUserIdAndShiftDateAndTimezone(
                    shift.getUserId(), shift.getShiftDate(), shift.getTimezone());
            if (existingShift != null) {
                shiftMapper.updateStatus(existingShift.getShiftId(), shift.getStatus().name());
            } else {
                shiftMapper.insert(shift);
            }
        }
    }

    @Transactional
    public void resetAdminShifts(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        List<Shift> shifts = shiftMapper.findByShiftDateBetween(startDate, endDate);
        for (Shift shift : shifts) {
            shiftMapper.updateStatus(shift.getShiftId(), Status.SUBMITTED.name());
        }
    }

    @Transactional
    public void saveShifts(Long userId, List<Shift> shifts) {
        List<Shift> shiftsToInsert = new ArrayList<>();
        for (Shift shift : shifts) {
            shift.setUserId(userId);
            if (shift.getStatus() == null) {
                shift.setStatus(Status.REQUESTED);
            }
            shiftsToInsert.add(shift);
        }
        shiftMapper.batchInsert(shiftsToInsert);
    }
} 