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
 1.ユーザー登録
 2.ログイン
 3.トークン検証
 */
@RestController //RestAPIコントローラー
@RequestMapping("/api/auth") //全共通のベースのURL
@CrossOrigin(origins = "*") //CORS設定（フロントエンドから全アクセス許可）
public class AuthController {

    private final AuthService authService;

    //コンストラクタインジェクションでauthServiceを自動で設定
    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /*
     1.ユーザー登録API
     POST /api/auth/register
     */
    @PostMapping("/register")
    //@Valid：バリデーション、@RequestBody：HTTPのJSONボディをオブジェクトに変換
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            System.out.println("ユーザー登録リクエスト受信: " + registerRequest.getUsername()); //デバッグログ追加取得用

            // リクエストDTOからUserエンティティを作成
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setPassword(registerRequest.getPassword());
            user.setBio(registerRequest.getBio());

            System.out.println("ユーザー登録処理開始"); //デバッグログ追加取得用
            // ユーザー登録（サービスクラスに移す）
            User registeredUser = authService.registerUser(user);
            System.out.println("ユーザー登録成功: " + registeredUser.getId()); //デバッグログ追加取得用

            // エンティティからレスポンス用DTOに変換
            UserResponse userResponse = new UserResponse(
                    registeredUser.getId(),
                    registeredUser.getUsername(),
                    registeredUser.getProfilePicture(),
                    registeredUser.getBio(),
                    registeredUser.getCreatedAt(),
                    registeredUser.getUpdatedAt()
            );

            //成功レスポンスを返す（HTTPステータス201 = Created）
            return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);

        } catch (RuntimeException e) {
            System.out.println("ユーザー登録エラー: " + e.getMessage()); //デバッグログ追加取得用
            e.printStackTrace(); // スタックトレース出力
            //エラーが発生した場合
            return ResponseEntity.badRequest().body("ユーザー登録に失敗しました: " + e.getMessage());
        }
    }

    /*
     2.ログインAPI
     POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            //認証処理とJWTトークン生成
            String jwt = authService.authenticateUser(loginRequest.getUsername(), loginRequest.getPassword());

            //認証成功時、ユーザー情報を返す
            User user = authService.getUserByUsername(loginRequest.getUsername());

            //レスポンス作成
            JwtResponse jwtResponse = new JwtResponse(jwt, user.getId(), user.getUsername());

            //成功レスポンスを返す（HTTPステータス200 = OK）
            return ResponseEntity.ok(jwtResponse);

        } catch (RuntimeException e) {
            //認証失敗の場合
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("ログインに失敗しました: " + e.getMessage());
        }
    }

    /*
     3.トークン検証API
     POST /api/auth/validate
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        try {
            //トークンの有効性をチェック
            boolean isValid = authService.validateToken(token);

            if (isValid) {
                return ResponseEntity.ok("トークンは有効です");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("トークンが無効です");
            }

        } catch (Exception e) {
            //予期しないエラーの場合
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("トークン検証中にエラーが発生しました");
        }
    }
}