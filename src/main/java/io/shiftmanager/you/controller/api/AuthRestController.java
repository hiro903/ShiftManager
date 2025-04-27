package io.shiftmanager.you.controller.api;
import io.shiftmanager.you.model.User;
import io.shiftmanager.you.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthRestController{
    
private final AuthenticationManager authenticationManager;
private final UserService UserService;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest){
        try{
            String email = loginRequest.get("email");
            String password = loginRequest.get("password");

            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = UserService.findByEmail(email);
            Map<String, Object> response = new HashMap<>();
            response.put("userId", user.getUserId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("active", user.isActive());
            response.put("admin", user.isAdmin());
            response.put("massage","ログインに成功しました");

            return ResponseEntity.ok(response);

        } catch (AuthenticationException e){
            Map<String, Object> response = new HashMap<>();
            response.put("error","認証に失敗しました");
            response.put("massage","メールアドレスまたはパスワードが間違っています");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    } 
}



 

