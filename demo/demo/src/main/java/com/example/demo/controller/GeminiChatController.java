package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.GeminiChatRequest; 
import com.example.demo.service.GenimiService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/gemini")
public class GeminiChatController {

    @Autowired
    private GenimiService genimiService;

    @PostMapping("/chat")
    // 2. (*** 修改 ***) 
    //    方法參數改用 GeminiChatRequest (它包含 List<GeminiMessageDto>)
    public String handleChat(
        @Valid @RequestBody GeminiChatRequest request
    ) {
        // 3. (*** 修改 ***) 
        //    呼叫 "新" 的 generateChatResponse 方法
        return genimiService.generateChatResponse(request.messages());
    }
}