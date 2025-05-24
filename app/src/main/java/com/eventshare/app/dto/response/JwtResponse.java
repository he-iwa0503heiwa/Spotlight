package com.eventshare.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
ログイン成功時のレスポンスのためのDTOクラス
フロントエンドにjwtトークンとユーザー情報を返すためのクラス
 */
public class JwtResponse {
    private String token;    //JWTトークン
    private String type = "Bearer";    //トークンの種類（固定値）
    private Long id;         //ユーザーID
    private String username; //ユーザー名

    /*
    カスタムコンストラクタ
     */
    public JwtResponse(String token, Long id, String username) {
        this.token = token;
        this.id = id;
        this.username = username;
    }
}