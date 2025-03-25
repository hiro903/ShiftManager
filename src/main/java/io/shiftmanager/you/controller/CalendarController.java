package io.shiftmanager.you.controller;

import io.shiftmanager.you.model.Shift;
import io.shiftmanager.you.model.User;
import io.shiftmanager.you.service.ShiftService;
import io.shiftmanager.you.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final ShiftService shiftService;
    private final UserService userService;

    @GetMapping
    public String showCalendar(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(authentication.getName());
        
        // 現在の月のシフトを取得
        LocalDate now = LocalDate.now();
        List<Shift> shifts = shiftService.getShiftsByMonth(user.getUserId(), now.getYear(), now.getMonthValue());
        
        // カレンダーデータの準備
        YearMonth yearMonth = YearMonth.of(now.getYear(), now.getMonth());
        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1 (Monday) to 7 (Sunday)
        if (dayOfWeek == 7) dayOfWeek = 0; // 日曜日は0にする
        
        List<List<CalendarDay>> calendarWeeks = new ArrayList<>();
        List<CalendarDay> week = new ArrayList<>();
        
        // 月の最初の日より前の空白を埋める
        for (int i = 0; i < dayOfWeek; i++) {
            week.add(new CalendarDay());
        }
        
        // 各日のカレンダーデータを作成
        Map<LocalDate, List<Shift>> shiftsMap = new HashMap<>();
        for (Shift shift : shifts) {
            shiftsMap.computeIfAbsent(shift.getShiftDate(), k -> new ArrayList<>()).add(shift);
        }
        
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = yearMonth.atDay(day);
            CalendarDay calendarDay = new CalendarDay(date, shiftsMap.getOrDefault(date, new ArrayList<>()));
            week.add(calendarDay);
            
            // 週の終わり、または月の終わりの場合は新しい週を開始
            if (week.size() == 7 || day == daysInMonth) {
                calendarWeeks.add(week);
                week = new ArrayList<>();
            }
        }
        
        // 最後の週の残りを空白で埋める
        while (week.size() > 0 && week.size() < 7) {
            week.add(new CalendarDay());
        }
        
        model.addAttribute("calendarWeeks", calendarWeeks);
        model.addAttribute("shifts", shifts);
        model.addAttribute("requests", shifts);
        model.addAttribute("currentUser", user);
        model.addAttribute("currentMonth", yearMonth);
        return "calendar";
    }

    @PostMapping("/shift/request")
    public String requestShift(@RequestParam LocalDate date,
                             @RequestParam String timezone,
                             RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(authentication.getName());
        
        try {
            shiftService.requestShift(user.getUserId(), date, timezone);
            redirectAttributes.addFlashAttribute("successMessage", "シフトを申請しました");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "シフトの申請に失敗しました");
        }
        
        return "redirect:/calendar";
    }

    @PostMapping("/shift/cancel")
    public String cancelShift(@RequestParam Long shiftId,
                            RedirectAttributes redirectAttributes) {
        try {
            shiftService.cancelShift(shiftId);
            redirectAttributes.addFlashAttribute("successMessage", "シフトをキャンセルしました");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "シフトのキャンセルに失敗しました");
        }
        
        return "redirect:/calendar";
    }
    
    @PostMapping("/shift/bulk-day")
    @ResponseBody
    public ResponseEntity<?> bulkDayRequest(@RequestParam int year,
                                          @RequestParam int month,
                                          @RequestParam List<Integer> weekdays,
                                          @RequestParam String timezone) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = userService.findByEmail(authentication.getName());
            
            Set<Integer> daysOfWeek = new HashSet<>(weekdays);
            shiftService.bulkRequestShiftByDayOfWeek(user.getUserId(), year, month, daysOfWeek, timezone);
            
            return ResponseEntity.ok().body(Map.of("success", true, "message", "一括登録が完了しました"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "一括登録に失敗しました: " + e.getMessage()));
        }
    }
    
    @PostMapping("/shift/reset")
    @ResponseBody
    public ResponseEntity<?> resetShifts(@RequestParam int year,
                                       @RequestParam int month) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = userService.findByEmail(authentication.getName());
            
            shiftService.resetShiftsForMonth(user.getUserId(), year, month);
            
            return ResponseEntity.ok().body(Map.of("success", true, "message", "シフトをリセットしました"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "リセットに失敗しました: " + e.getMessage()));
        }
    }
    
    @PostMapping("/shift/save")
    @ResponseBody
    public ResponseEntity<?> saveShifts(@RequestBody List<Shift> shifts) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = userService.findByEmail(authentication.getName());
            
            shiftService.saveShifts(user.getUserId(), shifts);
            
            return ResponseEntity.ok().body(Map.of("success", true, "message", "シフトを保存しました"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "保存に失敗しました: " + e.getMessage()));
        }
    }
    
    @PostMapping("/shift/submit")
    @ResponseBody
    public ResponseEntity<?> submitShifts(@RequestParam int year,
                                        @RequestParam int month) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = userService.findByEmail(authentication.getName());
            
            shiftService.submitShifts(user.getUserId(), year, month);
            
            return ResponseEntity.ok().body(Map.of("success", true, "message", "シフトを提出しました"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "提出に失敗しました: " + e.getMessage()));
        }
    }
    
    // カレンダー日付データを保持するクラス
    public static class CalendarDay {
        private LocalDate date;
        private List<Shift> shifts;
        private boolean isCurrentMonth;
        
        public CalendarDay() {
            this.isCurrentMonth = false;
        }
        
        public CalendarDay(LocalDate date, List<Shift> shifts) {
            this.date = date;
            this.shifts = shifts;
            this.isCurrentMonth = true;
        }
        
        public LocalDate getDate() {
            return date;
        }
        
        public List<Shift> getShifts() {
            return shifts;
        }
        
        public boolean isCurrentMonth() {
            return isCurrentMonth;
        }
        
        public int getDay() {
            return date != null ? date.getDayOfMonth() : 0;
        }
    }
} 