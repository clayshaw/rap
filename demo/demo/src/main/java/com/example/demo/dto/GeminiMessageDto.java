package com.example.demo.dto;

// 這個 'record' 必須和你前端 的 { role: '...', content: '...' } 格式一致
public record GeminiMessageDto(
    String role, // 'user' 或 'model'
    String content
) {}