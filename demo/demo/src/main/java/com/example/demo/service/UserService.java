package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;


@Service // 告訴 Spring 這是服務層
public class UserService {

    // 注入 (DI) 我們需要用到的工具
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    //建立註冊方法 (核心邏輯)
    public User registerUser(String username, String password, String email) {

        // 邏輯 A：檢查使用者名稱是否已被使用
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Error: Username is already taken!");
        }

        // 邏輯 B：檢查 Email 是否已被使用
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        // 建立新 User 物件
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);

        // 加密密碼
        String hashedPassword = passwordEncoder.encode(password);
        user.setPassword(hashedPassword);

        //儲存到資料庫
        return userRepository.save(user);
    }

    public String loginUser(String username, String password) {

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Error: User not found."));

        boolean isPasswordMatch = passwordEncoder.matches(password, user.getPassword());

        if (!isPasswordMatch) {
            throw new RuntimeException("Error: Invalid password.");
        }

        //如果都通過，產生並回傳 Token
        return jwtService.generateToken(username);
    }

    /**
     * 根據用戶名稱查找用戶
     * @param username 用戶名稱
     * @return User 物件
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Error: User not found."));
    }
    
}