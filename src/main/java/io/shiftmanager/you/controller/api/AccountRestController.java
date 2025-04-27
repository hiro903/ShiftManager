package io.shiftmanager.you.controller.api;

import io.shiftmanager.you.model.User;
import io.shiftmanager.you.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountRestController{

    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<?> createAccount(@RequestBody @Validated(User.Registration.class) User user){
        try{
            if(userService.existsByEmail(user.getEmail())){
                Map<String, String> response = new HashMap<>();
                response.put("error", "このメールアドレスは既に使用されています");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        user.setUsername(user.getEmail());
        user.setActive(true);
        user.setAdmin(false);

        User createdUser = userService.createUser(user);

        Map<String , Object> response = new HashMap<>();
        response.put("message", "アカウントが正常に作成されました");
        response.put("userId", createdUser.getUserId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (Exception e){
        Map<String, String> response = new HashMap<>();
        response.put("error", "アカウントの作成に失敗しました");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
}
}
