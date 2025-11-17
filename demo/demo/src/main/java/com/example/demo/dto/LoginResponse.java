package com.example.demo.dto;

// 回傳給前端的物件，只包含 token
public record LoginResponse(String token) {
}