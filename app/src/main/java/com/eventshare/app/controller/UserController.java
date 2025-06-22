package com.eventshare.app.controller;

import com.eventshare.app.entity.User;
import com.eventshare.app.service.EventCategoryService;
import com.eventshare.app.service.EventService;
import com.eventshare.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
//    @GetMapping("/me")
//    public ResponseEntity<?> getCurrentUser(){
//        try {
//            // 現在ログインしているユーザーを取得
//            String username = SecurityContextHolder.getContext().getAuthentication().getName();
//            User user = userService.getUserByUsername(username);
//
//            //パスワードを除いたユーザー情報のレスポンス
//            UserResponce userInfo = new UserResponce;
//        }
//    }
}
