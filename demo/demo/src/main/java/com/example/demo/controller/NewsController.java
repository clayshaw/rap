package com.example.demo.controller;

import com.example.demo.entity.News;
import com.example.demo.repository.NewsRepository;
import com.example.demo.service.PortfolioService; // <-- 1. 匯入 PortfolioService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // <-- 2. 匯入
import org.springframework.security.core.userdetails.UserDetails; // <-- 3. 匯入
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/news")
public class NewsController {

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private PortfolioService portfolioService;

    /**
     * 取得 "目前登入者" 的個人化新聞
     */
    @GetMapping
    public List<News> getMyNews(@AuthenticationPrincipal UserDetails userDetails) {

        // 取得登入者的 username
        String username = userDetails.getUsername();

        //呼叫 PortfolioService 取得 "['2330', '0050']"
        List<String> userSymbols = portfolioService.getUniqueStockSymbols(username);

        if (userSymbols.isEmpty()) {
            // 如果使用者沒有持股，回傳空列表
            return List.of(); 
        }

        // 呼叫 NewsRepository 查詢這些股票的新聞
        return newsRepository.findByStockSymbolInOrderByCreatedAtDesc(userSymbols);
    }
}