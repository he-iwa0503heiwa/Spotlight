package com.eventshare.app.controller;

import com.eventshare.app.config.TestSecurityConfig;
import com.eventshare.app.entity.User;
import com.eventshare.app.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * AuthControllerのテストクラス
 * ユーザー登録、ログイン、トークン検証の機能をテスト
 */
@WebMvcTest(AuthControllerTest.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    //@InjectMocksでコントローラーにモックを注入
    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;
    private User testUser;

}
