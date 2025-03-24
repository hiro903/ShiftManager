package io.shiftmanager.you.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequiredArgsConstructor
public class AuthController {

    @GetMapping("/login")
    public String showLoginForm(Model model, HttpSession session, HttpServletRequest request){
        String referrer = request.getHeader("Referrer");
        if (referrer != null && referrer.contains("/account/create")){
            return "login";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null &&
            authentication.isAuthenticated()&&
            !(authentication instanceof AnonymousAuthenticationToken)){
                return "redirect:/calendar";

            }
            return "login";
    }

    @GetMapping("/login-error")
    public String loginError(Model model){
        model.addAttribute("loginError",true);
        model.addAttribute("errorMessage","ユーザー名またはパスワードが間違っています");
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
