package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.ChatGptService;



@RestController
@RequestMapping("/api/chatgpt")
public class ChatGptController {
    @Autowired
    private ChatGptService chatGptService;

    @GetMapping("analsis")
    public String handleChat(

    ) {
        return chatGptService.generateResponse();
        // return "";
    }
    
}
