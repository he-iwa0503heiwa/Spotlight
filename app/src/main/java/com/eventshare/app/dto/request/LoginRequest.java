package com.eventshare.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 ログインリクエストのためのDTOクラス
 フロントエンドから送信される JSON データを受け取るためのクラス
 */
@Data                    // Lombokアノテーション：getter, setter, toString等を自動生成
@NoArgsConstructor       // Lombokアノテーション：引数なしコンストラクタを自動生成
@AllArgsConstructor      // Lombokアノテーション：全引数ありコンストラクタを自動生成
public class LoginRequest {
    @NotBlank(message = "ユーザー名は必須です")   //バリデーション：空白不可
    private String username;

    @NotBlank(message = "パスワードは必須です")    //バリデーション：空白不可
    private String password;
}