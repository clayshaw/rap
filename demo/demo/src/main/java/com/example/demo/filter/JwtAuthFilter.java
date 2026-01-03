package com.example.demo.filter;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    @Lazy
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request, 
        @NonNull HttpServletResponse response, 
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        //    如果請求是針對 /api/auth/ (登入或註冊)
        //    我們就 "跳過" 這個 JWT 驗證，直接放行
        if (request.getServletPath().contains("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        //取得 "Authorization" 標頭
        final String authHeader = request.getHeader("Authorization");
        
        //檢查標頭是否存在...
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); 
            return;
        }

        //取得 Token
        final String jwt = authHeader.substring(7); 
        
        // 從 Token 提取使用者名稱
        final String username = jwtService.extractUsername(jwt);
        
        // 如果成功提取到使用者名稱，且目前尚未驗證
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // 從資料庫載入該使用者的詳細資訊
            var userDetails = this.userDetailsService.loadUserByUsername(username);
            
            // 驗證 Token 是否有效
            if (jwtService.validateToken(jwt, userDetails)) {
                
                //建立一個「已驗證」的 Authentication 物件
                var authToken = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );
                
                // 設定額外的請求資訊
                authToken.setDetails(
                    new org.springframework.security.web.authentication.WebAuthenticationDetailsSource()
                        .buildDetails(request)
                );
                
                // 將此驗證結果放入 SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}