package com.example.demo.config;

import java.util.List; 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration; 
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy; 
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; 
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.demo.filter.JwtAuthFilter;
import com.example.demo.repository.UserRepository;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter; // 注入 Filter

    @Autowired
    private UserRepository userRepository; // 注入 Repository




    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    

    // 更新 filterChain ---
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(withDefaults()) // 啟用 CORS 設定
            .csrf(csrf -> csrf.disable()) // 關閉 CSRF
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**").permitAll() // 允許 /api/auth/ (註冊和登入)
                .anyRequest().authenticated() // 其他所有 API 都需要驗證
            )

            // 設定 Session 為 STATELESS (無狀態) ---
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))



            // 將 JWT Filter 放在預設的 Filter 之前 ---
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // UserDetailsService Bean ---
    // 告訴 Spring Security 如何去資料庫讀取使用者
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    //AuthenticationProvider Bean ---
    // 這是 Spring Security 的驗證機制
    

    //AuthenticationManager Bean ---
    // (登入時會用到)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允許來自 Vue 伺服器的請求
        // (請改成你 Vue 執行時的準確 URL，通常是 5173)
        configuration.setAllowedOrigins(List.of(
            "http://localhost:5173",
            "http://10.244.208.177:5173/",
            "http://192.168.0.102:5173/",
            "http://192.168.0.104:5173/"
            )); 
        
        // 允許所有請求方法 (GET, POST, PUT, DELETE...)
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        
        // 允許所有標頭 (Headers)
        configuration.setAllowedHeaders(List.of("*")); 

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 對所有 API 套用此設定
        return source;
    }
}