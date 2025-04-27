package io.shiftmanager.you.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;

@Data
@Alias("User")
public class User {
    private Long userId;

    @NotBlank(groups = Registration.class, message = "ユーザー名を入力してください")
    @Size(min = 2, max = 50, message = "ユーザー名は2文字以上50文字以下で入力してください")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "ユーザー名は英数字、ハイフン、アンダースコアのみ使用可能です")
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "パスワードを入力してください")
    @Size(min = 4, max = 20, message = "パスワードは4文字以上20文字以下で入力してください")
    private String password;

    @NotBlank(message = "メールアドレスを入力してください")
    @Email(message = "有効なメールアドレスを入力してください")
    @Size(max = 100, message = "メールアドレスは100文字以下で入力してください")
    private String email;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean active;
    private boolean admin;

    // バリデーショングループ用のマーカーインターフェース
    public interface Registration {}

    // Spring Security用のメソッド
    public boolean isAdmin() {
        return this.admin;
    }

    public boolean isActive() {
        return this.active;
    }
}
