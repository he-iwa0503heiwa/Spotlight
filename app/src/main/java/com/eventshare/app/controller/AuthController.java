package com.eventshare.app.controller;

import com.eventshare.app.dto.request.LoginRequest;
import com.eventshare.app.dto.request.RegisterRequest;
import com.eventshare.app.dto.response.JwtResponse;
import com.eventshare.app.dto.response.UserResponse;
import com.eventshare.app.entity.User;
import com.eventshare.app.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/*
 認証関連のAPIエンドポイントを提供するコントローラー
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /*
     ユーザー登録API
     POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // リクエストからUserエンティティを作成
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setPassword(registerRequest.getPassword());
            user.setBio(registerRequest.getBio());

            // ユーザー登録
            User registeredUser = authService.registerUser(user);

            // レスポンス用DTOに変換
            UserResponse userResponse = new UserResponse(
                    registeredUser.getId(),
                    registeredUser.getUsername(),
                    registeredUser.getProfilePicture(),
                    registeredUser.getBio(),
                    registeredUser.getCreatedAt(),
                    registeredUser.getUpdatedAt()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("ユーザー登録に失敗しました: " + e.getMessage());
        }
    }

    /*
     ログインAPI
     POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // 認証処理とJWTトークン生成
            String jwt = authService.authenticateUser(loginRequest.getUsername(), loginRequest.getPassword());

            // 認証成功時、ユーザー情報も返す（ユーザーサービスから取得する必要があるが、簡易的に実装）
            // TODO: ユーザー情報を取得するサービスメソッドを呼び出し

            // レスポンス作成（仮でユーザーIDは1、後で修正）
            JwtResponse jwtResponse = new JwtResponse(jwt, 1L, loginRequest.getUsername());

            return ResponseEntity.ok(jwtResponse);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("ログインに失敗しました: " + e.getMessage());
        }
    }

    /*
     トークン検証API
     POST /api/auth/validate
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        try {
            boolean isValid = authService.validateToken(token);

            if (isValid) {
                return ResponseEntity.ok("トークンは有効です");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("トークンが無効です");
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("トークン検証中にエラーが発生しました");
        }
    }
}