package com.eventshare.app.controller;

import com.eventshare.app.config.TestSecurityConfig;
import com.eventshare.app.dto.request.LoginRequest;
import com.eventshare.app.dto.request.RegisterRequest;
import com.eventshare.app.entity.User;
import com.eventshare.app.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/*
 * AuthControllerのテストクラス
 * ユーザー登録、ログイン、トークン検証の機能をテスト
 */
@WebMvcTest(AuthControllerTest.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;//テスト用の仮想的なWebサーバー

    @Mock
    private AuthService authService;

    //@InjectMocksでコントローラーにモックを注入
    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;//JSON変換ツール
    private User testUser;

    //テストメソッド実行前に実行
    @BeforeEach
    void setUp() {
        //objectMapperの設定
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        //MockMvcを手動で構築
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();//テスト対象のコントローラーを設定

        //テスト用ユーザーデータ作成
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setPassword("password");
        testUser.setBio("テストユーザーです");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
    }
    /*
    ユーザー登録成功のテスト
     */
    @Test
    void testRegisterUser_Success() throws Exception{
        //テストデータの準備
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setBio("新規ユーザーです");
        //モックの動作定義（@Mockを使用）
        when(authService.registerUser(any(User.class))).thenReturn(testUser);

        //実行と検証
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))//objectMapperでjavaからjsonに変換
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    /*
    ユーザー登録失敗のテスト（既存ユーザー名）
     */
    @Test
    void testRegisterUser_UsernameAlreadyExists() throws Exception {
        //準備
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("exsitinguser");
        registerRequest.setPassword("password123");
        registerRequest.setBio("既存ユーザーです");
        //モックで例外をスロー
        when(authService.registerUser(any(User.class)))
                .thenThrow(new RuntimeException("このユーザーはすでに存在します"));

        //実行と検証
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    /*
    ログイン成功のテスト
     */
    @Test
    void testLoginUser_Success() throws Exception{
        //準備
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
        //トークン準備
        String testToken = "mockyounotekito.text.token";
        //モックの設定
        when(authService.authenticateUser("testuser", "password123"))
                .thenReturn(testToken);
        when(authService.getUserByUsername("testuser"))
                .thenReturn(testUser);

        //実行と検証
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(testToken))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    /*
    ログイン失敗テスト
     */
    @Test
    void testLoginUser_AuthenticationFailure() throws Exception{
        //準備
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        //モック設定(認証失敗)
        when(authService.authenticateUser("testuser", "wrongpassword"))
                .thenThrow(new RuntimeException("認証に失敗しました"));

        //検証
        mockMvc.perform(post("api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}
