package com.example.demo.entity;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "users") // 建議表名稱用複數
public class User implements UserDetails{

    @Id // 標記為主鍵
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 標記為自動增長
    private Long id;

    @Column(nullable = false, unique = true, length = 50) // 標記為欄位、不可為空、唯一
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String password; // 這裡會儲存加密後的密碼

    @Column(unique = true, length = 100)
    private String email;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();
    
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER")); 
    }



    @Override
    public boolean isAccountNonExpired() {
        // 帳號是否未過期
    
        return true; 
    }

    @Override
    public boolean isAccountNonLocked() {
        // 帳號是否未鎖定
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // 密碼是否未過期
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 帳號是否啟用
        return true;
    }
}