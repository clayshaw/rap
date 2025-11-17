package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.PortfolioRequest;
import com.example.demo.entity.Portfolio;
import com.example.demo.entity.User;
import com.example.demo.repository.PortfolioRepository;
import com.example.demo.repository.UserRepository;

@Service
public class PortfolioService {

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 根據使用者名稱，取得該使用者的所有持股
     */
    public List<Portfolio> getPortfolioByUsername(String username) {
        // 透過 username 找到 User 物件
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // 透過 userId 找到所有 Portfolio 紀錄
        return portfolioRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    /**
     * 為指定的使用者新增一筆持股紀錄
     */
    public Portfolio addPortfolio(String username, PortfolioRequest dto) {
    
        // 找到 User
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // 手動將 DTO 轉換為 Entity
        Portfolio newPortfolio = new Portfolio();
        newPortfolio.setStockSymbol(dto.stockSymbol()); // 從 DTO (record) 取得資料
        newPortfolio.setQuantity(dto.quantity());
        newPortfolio.setPurchasePrice(dto.purchasePrice());

        // 綁定 User 到這筆 Portfolio 紀錄
        newPortfolio.setUser(user);

        // 儲存到資料庫
        return portfolioRepository.save(newPortfolio);
    }

    public List<Portfolio> getPortfolioHistory(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // 呼叫我們剛才修改的 "排序" 方法
        return portfolioRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }
    public List<String> getUniqueStockSymbols(String username) {
        // 1. 取得使用者的 "交易歷史" (這你已經寫好了)
        List<Portfolio> portfolioHistory = getPortfolioHistory(username);

        // 2. 透過 Java Stream 取得不重複 (distinct) 的股票代號
        return portfolioHistory.stream()
                .map(Portfolio::getStockSymbol) // 轉換: (Portfolio -> "2330")
                .distinct() // 去除重複
                .toList(); // 轉換回 List<String>
    }
    
}