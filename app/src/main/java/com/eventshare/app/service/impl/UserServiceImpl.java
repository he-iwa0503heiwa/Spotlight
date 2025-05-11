package com.eventshare.app.service.impl;

import com.eventshare.app.entity.User;
import com.eventshare.app.repository.UserRepository;
import com.eventshare.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    //リポジトリの依存性注入
    private final UserRepository userRepository;

    //コンストラクタインジェクション
    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    //以下メソッド実装
    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません。ID: " + id));
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません。ユーザー名: " + username));
    }

    @Override
    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("そのユーザー名はすでに存在します：　" + user.getUsername());
        }
        //ここでパスワードのハッシュ化などの処理を行う（後で追加）
        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long id, User user) {
        //更新対象を取得
        User existingUser = getUserById(id);

        //「変更がある」かつ「すでに存在している」場合は例外をスロー
        if (!existingUser.getUsername().equals(user.getUsername()) && userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("そのユーザー名はすでに存在します：　" + user.getUsername());
        }
        existingUser.setUsername(user.getUsername());
        //パスワードが変更されたらハッシュ化する（後で追加）
        existingUser.setBio(user.getBio());
        existingUser.setProfilePicture(user.getProfilePicture());//今は使わないが、追加しておく。

        return userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(Long id) {
        //更新対象を取得
        User user = getUserById(id);
        userRepository.delete(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
}