package com.eventshare.app.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/*
 HTTPリクエストごとにJWTトークンを検証し、SecurityContextに認証情報を設定するフィルター
 ログイン済みかどうかチェックするフィルター
 */
//1リクエストにつき1回だけ動作するフィルター
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    //JwtAuthenticationFilterのログを出力
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    //JWTトークンの検証に使用
    private final JwtTokenProvider tokenProvider;
    //ユーザー情報をデータベースから取得する
    private final UserDetailsService userDetailsService;

    //コンストラクタインジェクション
    @Autowired
    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    /*
    リクエストごとにJWTトークンの検証と認証を行う
     */
    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            //トークンが空でなく、かつ期限切れや改ざんがなく正しいか
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                //トークン内にあるユーザー名を取り出す
                String username = tokenProvider.getUsernameFromToken(jwt);

                //上記でとってきたユーザー名をもとにDBなどから詳細情報をロード
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                //ユーザー詳細をもとに認証済みである証明のオブジェクトを作成
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                //リモートIPアドレスやセッションIDなどの認証の詳細情報をセット
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                //認証オブジェクトをSpring Securityに登録
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("認証情報をセキュリティコンテキストに設定できませんでした", ex);
        }

        //認証の有無に関係なく、次のフィルターへ処理を進める（認証失敗の場合は次の処理で弾かれる）
        filterChain.doFilter(request, response);
    }

    /*
    リクエストのヘッダーからJWTトークンを抽出するメソッド
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        //Authorizationヘッダーを取得
        String bearerToken = request.getHeader("Authorization");
        //トークン(ヘッダー)が存在している　かつ"Bearer " で始まっている
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            //"Bearer " の後ろだけを切り取って返す
            return bearerToken.substring(7);
        }
        return null;
    }
}