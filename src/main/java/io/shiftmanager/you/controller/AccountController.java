package io.shiftmanager.you.controller;

import io.shiftmanager.you.model.User;
import io.shiftmanager.you.service.UserService;
import io.shiftmanager.you.exception.DuplicateEmailException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final UserService userService;

    @GetMapping("/create")
    public String showCreateForm(Model model){
        // まだユーザー情報が画面にない場合は、新しい空のユーザー情報を作ります
        if (!model.containsAttribute("user")){
            model.addAttribute("user",new User());
        }
        // account-create.html という画面を表示します
        return "account-create";
    }
    	// アカウント作成フォームが送信された時の処理
       // /account/create に対してPOSTリクエストがあった時に動きます
@PostMapping("/create")
    public String createAccount(
        // フォームの入力内容をUserクラスの形式で受け取ります
        // @Validは入力内容が正しいかチェックする指示です
        @Valid @ModelAttribute("user") User user,
        // 入力チェックの結果をこの変数に入れます
        BindingResult result,
         // 画面遷移時にメッセージを渡すための機能
         Model model,
         RedirectAttributes redirectAttributes){

            // 入力内容にエラーがある場合
        if (result.hasErrors()) {
            model.addAttribute("errorMessage", "入力内容に誤りがあります");
            // アカウント作成画面を再表示します（エラーメッセージ付き）
            return "account-create";
        }
         
        try {
            // UserServiceを使って新しいユーザーを作成します
            userService.createUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "アカウントが正常に作成されました");
            // ログイン画面に移動します
            return "redirect:/login";

        } catch (DuplicateEmailException e) {
            model.addAttribute("errorMessage", "このメールアドレスは既に使用されています");
            return "account-create";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "アカウントの作成に失敗しました");
            return "account-create";
        }
    }
}
