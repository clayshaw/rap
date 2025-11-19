package com.example.demo.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.GeminiChatRequest; 
import com.example.demo.dto.GeminiMessageDto;
import com.example.demo.service.GenimiService;
import com.example.demo.service.PortfolioService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/gemini")
public class GeminiChatController {

    @Autowired
    private GenimiService genimiService;

    @Autowired
    private PortfolioService portfolioService;


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

    @GetMapping("recommendations")
    public List<String> getRecommendations(@AuthenticationPrincipal UserDetails userDetails) {

        // 取得登入者的 username
        String username = userDetails.getUsername();

        //呼叫 PortfolioService 取得 "['2330', '0050']"
        List<String> userSymbols = portfolioService.getUniqueStockSymbols(username);

        if (userSymbols.isEmpty()) {
            // 如果使用者沒有持股，回傳空列表
            return  List.of();
        }

        // 呼叫 GenimiService 查詢這些股票的推薦理由
        List<String> recommendations = new ArrayList<>();
        for (String symbol : userSymbols) {
            System.out.println("取得推薦理由 for stock: " + symbol);
            List<GeminiMessageDto> message = List.of(
                new GeminiMessageDto("user", "請分析近日 " + symbol + ".TW 新聞,價格,技術指標等等消息分析走勢 請用50字內中文說明")
            );
            recommendations.add(symbol+"\n"+genimiService.generateChatResponse(message));
        }
         // 這裡需要根據實際需求返回適當的結果
         return recommendations;
    }
    
}