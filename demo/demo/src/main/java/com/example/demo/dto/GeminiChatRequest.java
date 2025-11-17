package com.example.demo.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

// 請求的 Body 應該包含 "所有" 的歷史訊息
public record GeminiChatRequest(
    
    @NotEmpty
    List<GeminiMessageDto> messages
) {}