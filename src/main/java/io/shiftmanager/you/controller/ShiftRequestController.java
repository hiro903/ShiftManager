package io.shiftmanager.you.controller;

import io.shiftmanager.you.model.ShiftRequest;
import io.shiftmanager.you.model.User;
import io.shiftmanager.you.service.ShiftRequestService;
import io.shiftmanager.you.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/shifts")
@RequiredArgsConstructor
public class ShiftRequestController {

    private final ShiftRequestService shiftRequestService;
    private final UserService userService;

    @GetMapping("/calendar")
    public String showCalendar(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(authentication.getName());
        
        // 現在の月のシフト希望を取得
        LocalDate now = LocalDate.now();
        List<ShiftRequest> requests = shiftRequestService.getRequestsByMonth(user.getUserId(), now.getYear(), now.getMonthValue());
        
        model.addAttribute("requests", requests);
        model.addAttribute("currentUser", user);
        return "calendar";
    }

    @PostMapping("/request")
    public String requestShift(@RequestParam LocalDate date,
                             @RequestParam String timezone,
                             RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(authentication.getName());
        
        try {
            shiftRequestService.requestShift(user.getUserId(), date, timezone);
            redirectAttributes.addFlashAttribute("successMessage", "シフトを申請しました");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/shifts/calendar";
    }

    @PostMapping("/cancel")
    public String cancelRequest(@RequestParam Long requestId,
                              RedirectAttributes redirectAttributes) {
        try {
            shiftRequestService.cancelRequest(requestId);
            redirectAttributes.addFlashAttribute("successMessage", "シフト申請をキャンセルしました");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/shifts/calendar";
    }

    @PostMapping("/submit")
    public String submitRequests(RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(authentication.getName());
        
        try {
            LocalDate now = LocalDate.now();
            shiftRequestService.submitRequests(user.getUserId(), now.getYear(), now.getMonthValue());
            redirectAttributes.addFlashAttribute("successMessage", "シフト希望を提出しました");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/shifts/calendar";
    }
} 