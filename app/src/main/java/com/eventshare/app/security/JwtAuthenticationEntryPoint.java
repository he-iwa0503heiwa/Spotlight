package com.eventshare.app.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/*
未認証のリクエスト（ログインしていない人がログインが必要なAPIにアクセス）に自動で401を返すエラーハンドリングクラス
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    //エラーやデバッグ情報をログに記録するためのオブジェクト
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    /*
    認証されていないリクエストに対して401を返す
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        logger.error("認証されていないリクエスト: {}", authException.getMessage());
        //401ステータスコードとエラーメッセージを返却
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "認証情報が無効または欠落しています");
    }
}