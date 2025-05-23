package com.eventshare.app.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/*
JWTトークンの生成と検証を行うクラス
 */
@Component
public class JwtTokenProvider {
    /*
    フィールド変数の設定
     */
    //エラーやデバッグ情報をログに記録するためのオブジェクト
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    //トークンの署名に使用する秘密鍵(application.propertiesから)
    @Value("${app.jwt.secret:defaultSecretKey}")
    private String jwtSecret;

    //トークンの有効期限(application.propertiesから)
    @Value("${app.jwt.expiration:86400000}")
    private int jwtExpirationInMs;

    /*
    秘密鍵をSecretKeyオブジェクトに変換するメソッド
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /*
    認証トークンをもとにjwtトークンを生成するメソッド
     */
    public String generateToken(Authentication authentication) {
        //認証情報からユーザー情報を取得
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        //現在の日時と有効期限の設定
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + jwtExpirationInMs);

        //○Jwtトークンの構築
        return Jwts.builder()
                .setSubject(userDetails.getUsername()) //subject(ユーザー名)を設定
                .setIssuedAt(now) //発行時刻を設定
                .setExpiration(expireDate) //有効期限を設定
                .signWith(getSigningKey(), SignatureAlgorithm.HS512) //指定したアルゴリズム（HS512）と秘密鍵でトークンに署名
                .compact(); //最終的なJWT文字列を生成して返却
    }

    /*
    Jwtトークンからユーザー名を取得する
     */
    public String getUsernameFromToken(String token) {
        //トークンを解析してクレームを取得:キーと値のペアで構成される（JSON形式）
        Claims claims = Jwts.parserBuilder() //JWTパーサーオブジェクト(解析)を作成
                .setSigningKey(getSigningKey()) //パーサーに署名検証用の鍵を設定
                .build()
                .parseClaimsJws(token) //JWTトークン文字列を解析し、検証する
                .getBody(); //解析結果からペイロード部分（Claims）を取得

        return claims.getSubject(); //トークン作成時に設定したsubject（ユーザー名）を取得して返却
    }

    /*
    JWTトークンのバリデーションチェック
    (トークンをパースして署名を検証し、正常だったらtrueを返却)
     */
    public boolean validateToken(String token) {
        try {
            //トークンの署名と中身をパースして検証する（例外が出なければOK）
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException ex) { //署名が不正（改ざんされたトークン）
            logger.error("無効なJWT署名");
        } catch (MalformedJwtException ex) { //トークンの形式が不正
            logger.error("不正なJWTトークン");
        } catch (ExpiredJwtException ex) { //トークンの有効期限切れ
            logger.error("期限切れのJWTトークン");
        } catch (UnsupportedJwtException ex) { //サポートされていない形式のトークン
            logger.error("サポートされていないJWTトークン");
        } catch (IllegalArgumentException ex) { //空や無効な引数
            logger.error("空または無効なJWTクレーム");
        }
        return false;
    }
}