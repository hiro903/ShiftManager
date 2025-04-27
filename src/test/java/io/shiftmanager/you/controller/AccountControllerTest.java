package io.shiftmanager.you.controller;

import io.shiftmanager.you.config.TestConfig;
import io.shiftmanager.you.model.User;
import io.shiftmanager.you.service.UserService;
import io.shiftmanager.you.exception.DuplicateEmailException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.junit.jupiter.api.BeforeEach;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * アカウント作成画面のコントローラーをテストするクラス
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfig.class)
public class AccountControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    // アカウント作成フォーム表示の正常系をテスト
    @Test
    public void showCreateForm_Success() throws Exception {
        mockMvc.perform(get("/account/create")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account-create"))
                .andExpect(model().attributeExists("user"));
    }

    // アカウント作成の正常系をテスト
    @Test
    public void create_Success() throws Exception {
        when(userService.existsByEmail(any())).thenReturn(false);
        User newUser = new User();
        newUser.setUsername("testuser");
        newUser.setEmail("test@example.com");
        when(userService.createUser(any(User.class))).thenReturn(newUser);

        mockMvc.perform(post("/account/create")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .param("username", "testuser")
                .param("email", "test@example.com")
                .param("password", "Test@123")
                .param("confirmPassword", "Test@123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    // バリデーションエラー時の処理をテスト
    @Test
    public void create_ValidationError() throws Exception {
        mockMvc.perform(post("/account/create")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .param("username", "")
                .param("email", "invalid-email")
                .param("password", "short")
                .param("confirmPassword", "different"))
                .andExpect(status().isOk())
                .andExpect(view().name("account-create"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    // メールアドレス重複時の処理をテスト
    @Test
    public void create_DuplicateEmail() throws Exception {
        doThrow(new DuplicateEmailException("このメールアドレスは既に使用されています"))
            .when(userService).createUser(any(User.class));

        mockMvc.perform(post("/account/create")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .param("username", "existinguser")
                .param("email", "existing@example.com")
                .param("password", "Test@123")
                .param("confirmPassword", "Test@123"))
                .andExpect(status().isOk())
                .andExpect(view().name("account-create"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("errorMessage"));
    }
}
