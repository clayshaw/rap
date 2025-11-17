package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;



@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public User registerUser(@RequestBody RegisterRequest request) {
        return userService.registerUser(
            request.username(), 
            request.password(), 
            request.email()
        );
    }

    @PostMapping("/login")
    public LoginResponse loginUser(@RequestBody LoginRequest request) {

        //呼叫 Service 取得 Token (String)
        String token = userService.loginUser(
            request.username(), 
            request.password()
        );

        // 把 Token 包裝成 DTO 再回傳
        return new LoginResponse(token);
    }
}