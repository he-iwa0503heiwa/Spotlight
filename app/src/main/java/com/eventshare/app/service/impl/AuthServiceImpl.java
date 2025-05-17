package com.eventshare.app.service.impl;

import com.eventshare.app.entity.User;
import com.eventshare.app.repository.UserRepository;
import com.eventshare.app.security.JwtTokenProvider;
import com.eventshare.app.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    //リポジトリの依存性の注入
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;//パスワードのハッシュ化と検証（spring security）
    private final AuthenticationManager authenticationManager;//認証処理（spring security）
    private final JwtTokenProvider jwtTokenProvider;//JWT(Json Web Token)の生成と検証

    //コンストラクタインジェクション
    @Autowired
    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public User registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("このユーザー名はすでに存在します：　" + user.getUsername());
        }
        //spring securityを使用したパスワードのハッシュ化
        user.setPassword(passwordEncoder.encode(user.getPassword));

        return userRepository.save(user);
    }

    @Override
    //ユーザーの身元を確認しアクセス許可証（トークン）を発行する
    public String authenticateUser(String username, String password) {
        //Spring Securityの認証処理
        Authentication authentication = AuthenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        //上記で認証した処理をセキュリティコンテキストに設定（一時的に保存するメモリスペース）
        SecurityContextHolder.getContext().setAuthentication(authentication);

        //認証情報をもとにJWTトークンを生成
        return jwtTokenProvider.generateToken(authentication);
    }

    @Override
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }
}