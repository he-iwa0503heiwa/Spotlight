package com.eventshare.app.service;

import com.eventshare.app.entity.User;

import java.util.List;

public interface UserService {
    //全ユーザーを取得
    List<User> getAllUsers();

    //指定されたIDのユーザーを取得
    User getUserById(Long id);

    //指定された名前のユーザーを取得
    User getUserByUsername(String username);

    //ユーザー作成
    User createUser(User user);

    //ユーザー更新
    User updateUser(Long id, User user);

    //ユーザー削除
    void deleteUser(Long id);

    //指定された名前のユーザーが存在するかチェック
    boolean existsByUsername(String username);

    //ユーザー情報保存
    User saveUser(User user);
}