package com.eventshare.app.service;

import com.eventshare.app.entity.User;

public interface AuthService {
    //ユーザー登録
    User registerUser(User user);

    //ログイン認証
    String authenticateUser(String username, String password);

    //認証するトークンの有効性を検証
    boolean validateToken(String token);

    //ユーザー名からユーザーを取得
    User getUserByUsername(String username);
}