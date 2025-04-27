package io.shiftmanager.you.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.shiftmanager.you.model.User;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    @GetMapping("/login")
    public String showLoginForm(Model model, @RequestParam(value = "error", required = false) String error) {
        // すでにログイン済みの場合は適切なダッシュボードにリダイレクト
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && 
            authentication.isAuthenticated() && 
            !(authentication instanceof AnonymousAuthenticationToken)) {
            
            // 管理者かどうかに基づいてリダイレクト
            if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/calendar";
            }
        }
        
        // ログインエラーの処理
        if (error != null) {
            if ("access_denied".equals(error)) {
                model.addAttribute("loginError", true);
                model.addAttribute("errorMessage", "このページにアクセスする権限がありません");
            }
        }
        
        model.addAttribute("user", new User());
        return "login";
    }

    @GetMapping("/login-error")
    public String loginError(Model model) {
        log.warn("ログイン失敗: メールアドレスまたはパスワードが間違っています");
        model.addAttribute("loginError", true);
        model.addAttribute("errorMessage", "メールアドレスまたはパスワードが間違っています");
        model.addAttribute("user", new User());
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response,
                        RedirectAttributes redirectAttributes){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null){
            new SecurityContextLogoutHandler().logout(request, response, auth);
            redirectAttributes.addFlashAttribute("message","ログアウトしました");

        }
        return "redirect:/login";
    }
 
    @GetMapping("/access-denied")
    public String accessDenied(){ return "error/403";}

}
