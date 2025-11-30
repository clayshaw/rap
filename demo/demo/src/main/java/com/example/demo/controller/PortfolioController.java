package com.example.demo.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

import com.example.demo.dto.PortfolioRequest;
import com.example.demo.entity.Portfolio;
import com.example.demo.service.PortfolioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    @Autowired
    private PortfolioService portfolioService;

    @GetMapping
    public List<Portfolio> getMyPortfolio(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        return portfolioService.getPortfolioByUsername(username);
    }


    @PostMapping
    public Portfolio addPortfolioItem(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody PortfolioRequest portfolioRequest 
    ) {
        String username = userDetails.getUsername();
        //將 DTO 傳遞給 Service
        return portfolioService.addPortfolio(username, portfolioRequest);
    }


    @GetMapping("/history")
    public List<Portfolio> getMyPortfolioHistory(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        return portfolioService.getPortfolioHistory(username);
    }

    @DeleteMapping("/remove")
    public void deletePortfolioItem(
        @RequestBody Map<String, Instant> payload, // 使用 Map 接收 JSON 物件
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        Instant createdAt = payload.get("createdAt"); // 從 Map 取出
        
        System.out.println("Received createdAt for deletion: " + createdAt);
        String username = userDetails.getUsername();
        portfolioService.removePortfolioItem(username, createdAt);
    }
}