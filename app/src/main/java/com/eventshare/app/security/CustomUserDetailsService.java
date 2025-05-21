package com.eventshare.app.security;

import com.eventshare.app.entity.User;
import com.eventshare.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/*
 Spring SecurityのUserDetailsServiceインターフェースの実装クラス
 データベースからユーザー情報を取得し、認証のためのUserDetailsを提供する
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /*
    ユーザー名をユーザー情報から取得して、SpringSecurityが理解できるようにUserDetailsオブジェクトに変換する
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //ユーザー名をリポジトリから取得
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("ユーザー名が見つかりません" + username));

        //取得したユーザー名をUserDetailsオブジェクトに変換して返す
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername()) //ユーザー名設定
                .password(user.getPassword()) //パスワード設定
                .authorities(Collections.emptyList()) //権限設定
                .accountExpired(false) //有効期限設定
                .accountLocked(false) //ロック状態設定
                .credentialsExpired(false) //認証情報の期限切れ状態の設定
                .disabled(false) //無効化状態の設定
                .build(); //ユーザーオブジェクトを実装し実装
    }
}