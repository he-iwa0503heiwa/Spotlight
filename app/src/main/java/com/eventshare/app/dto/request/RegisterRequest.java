package com.eventshare.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 ユーザー登録リクエストのためのDTOクラス
 フロントエンドから送信されるユーザー登録データを受け取るためのクラス
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "ユーザー名は必須です")
    @Size(min = 3, max = 50, message = "ユーザー名は3文字以上50文字以下で入力してください")
    private String username;

    @NotBlank(message = "パスワードは必須です")
    @Size(min = 8, max = 50, message = "パスワードは6文字以上50文字以下で入力してください")
    private String password;

    @Size(max = 1000, message = "自己紹介は1000字以内で入力してください")
    private String bio;
}