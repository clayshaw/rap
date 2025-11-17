package com.example.demo.service;

import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    //  "JWT Secret Key"！

    public static final String SECRET = "Gu6F3CWnrMGfintu2tC0q+FF4rBCl2Jg3BTypvADkeM=";

    // 產生 Token 的方法
    public String generateToken(String username) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        // 設定 Token 過期時間 (例如 24 小時)
        long expirationMillis = nowMillis + 1000 * 60 * 60; // 1 小時
        Date expirationDate = new Date(expirationMillis);

        return Jwts.builder()
            .subject(username)      
            .issuedAt(now)          
            .expiration(expirationDate) 
            .signWith(getSignKey()) // <-- 新 (演算法會從 Key 自動推斷)
            .compact();
    }

    //取得簽章用的 Key (從 Base64 Secret 解碼)
    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // 檢查 Token 是否過期
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // 從 Token 中提取使用者名稱
    public String extractUsername(String token) {
        // "Subject" 就是我們在 generateToken 時放進去的 username
        return extractClaim(token, Claims::getSubject); 
    }

    // 從 Token 中提取過期時間
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    //  從 Token 中提取"單一"資料 (Claim)
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    //解析"整個" Token，取得所有資料 (Claims)
    private Claims extractAllClaims(String token) {
        return Jwts
            .parser() 
            .verifyWith(getSignKey())
            .build()
            .parseSignedClaims(token) 
            .getPayload();
    }
}