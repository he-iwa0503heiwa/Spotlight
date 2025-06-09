package com.eventshare.app.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Securityの「設定だけ」をまとめたクラス
 * 各securityパッケージで設定したSpring Securiryをbean化して他のクラスでも注入できるように設定しているクラス
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    //不正アクセスがあったときの処理をハンドリングするクラス：未認証の際は401を返す
    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    //JWTトークンプロバイダー
    private final JwtTokenProvider jwtTokenProvider;
    //ユーザー詳細サービス
    private final CustomUserDetailsService customUserDetailsService;

    @Autowired
    public SecurityConfig(JwtAuthenticationEntryPoint unauthorizedHandler,
                          JwtTokenProvider jwtTokenProvider,
                          CustomUserDetailsService customUserDetailsService) {
        this.unauthorizedHandler = unauthorizedHandler;
        this.jwtTokenProvider = jwtTokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    /*
    以下自作のクラスをspringが使えるようにbean化
     */
    //jwtトークンを検証するフィルター
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService);
    }

    //パスワードエンコーダー
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //認証マネージャー
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    //セキュリティフィルターチェーンの設定（HttpSecurity：「どのURLに誰がアクセスできるか」「認証はどうするか」などを設定するオブジェクト）
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))//フロントと接続のためのcors
                .csrf(csrf -> csrf.disable()) //CSRFを無効にする
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler)) //例外処理（未認証のときの対応）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //セッションの管理方法を「ステートレス」に
                .authorizeHttpRequests(auth -> auth //↓アクセス制御
                        .requestMatchers("/", "/index.html", "/*.js", "/*.css").permitAll()
                        .requestMatchers("/api/auth/**").permitAll() //api/auth/**は誰でもアクセス可
                        .requestMatchers("/api/public/**").permitAll() //api/public/**は誰でもアクセス可
                        .requestMatchers(HttpMethod.GET, "/api/events").permitAll() //イベント一覧は誰でも閲覧可
                        .requestMatchers(HttpMethod.GET, "/api/events/*").permitAll() //イベント詳細は誰でも閲覧可
                        .anyRequest().authenticated() //それ以外はアクセス不可
                );

        //JWTをpring Securityのフィルターの前にする（リクエストが来たときに、ログインチェックの前にトークンの検証を先に実行）
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        //↑の設定をもとにSecurityFilterChainを作成→適用
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));//ドメインアクセス許可
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));//メソッドアクセス許可
        configuration.setAllowedHeaders(Arrays.asList("*"));//ヘッダーアクセス許可
        configuration.setAllowCredentials(true);//認証トークン許可

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);//「/api/**のすべてのパスに適用」
        return source;
    }
}