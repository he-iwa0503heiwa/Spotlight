package com.eventshare.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
ユーザー情報レスポンスのためのDTOクラス
フロントエンドにユーザー情報を返すためのクラス
パスワードなどの機密情報は含まない
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;                    //ユーザーID
    private String username;            //ユーザー名
    private String profilePicture;      //プロフィール画像のパス
    private String bio;                 //自己紹介
    private LocalDateTime createdAt;    //作成日時
    private LocalDateTime updatedAt;    //更新日時
}