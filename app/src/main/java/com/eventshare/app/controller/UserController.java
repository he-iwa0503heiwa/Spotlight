package com.eventshare.app.controller;

import com.eventshare.app.entity.User;
import com.eventshare.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/*
ユーザー情報取得の
1.現在ログイン中のユーザー情報を取得
2.ユーザー情報の更新
 */
@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService){
        this.userService = userService;
    }

    /*
    1.現在ログイン中のユーザー情報を取得
    GET /api/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(){
        try {
            // 現在ログインしているユーザーを取得
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByUsername(username);

            //パスワードを除いたユーザー情報のレスポンス
            UserInfoResponse userInfo = new UserInfoResponse();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setBio(user.getBio());
            userInfo.setCreatedAt(user.getCreatedAt());

            return ResponseEntity.ok(userInfo);

        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body("ユーザー情報の取得に失敗しました：　" + e.getMessage());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("ユーザー情報の取得中にエラーが発生しました：" + e.getMessage());
        }
    }

    /*
    2.ユーザー情報の更新
    PUT /api/user/me
     */
    @PutMapping("/me")
    public ResponseEntity<?> updateCurrentUser(@RequestBody UserUpdateRequest request){
        try{
            // 現在ログインしているユーザーを取得
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByUsername(username);

            //自己紹介の更新
            if (request.getBio() != null){
                user.setBio(request.getBio());
            }

            //ユーザー情報を保存
            User updatedUser = userService.saveUser(user);

            //更新された情報をレスポンスへ
            UserInfoResponse userInfo = new UserInfoResponse();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setBio(user.getBio());
            userInfo.setCreatedAt(user.getCreatedAt());

            return ResponseEntity.ok(userInfo);
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body("ユーザー情報の更新に失敗しました：　" + e.getMessage());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("ユーザー情報の更新中エラーが発生しました：　" + e.getMessage());
        }
    }

    /*
    ユーザー情報表示用のレスポンス内部クラス
    パスワードなどの情報は含まない
     */
    public static class UserInfoResponse{
        private Long id;
        private String username;
        private String bio;
        private LocalDateTime createdAt;

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getBio() {
            return bio;
        }

        public void setBio(String bio) {
            this.bio = bio;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }

    /*
    ユーザー情報更新用のリクエスト内部クラス
     */
    public static class UserUpdateRequest{
        private String bio;

        //getter setter
        public String getBio(){
            return bio;
        }
        public void setBio(String bio){
            this.bio = bio;
        }
    }
}
