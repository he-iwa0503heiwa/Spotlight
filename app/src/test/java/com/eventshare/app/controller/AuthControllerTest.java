package com.eventshare.app.controller;

import com.eventshare.app.config.TestSecurityConfig;
import com.eventshare.app.entity.User;
import com.eventshare.app.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDateTime;

/**
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
}
