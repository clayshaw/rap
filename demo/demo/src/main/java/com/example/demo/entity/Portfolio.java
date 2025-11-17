package com.example.demo.entity;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "portfolios")
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- 建立關聯 ---
    @ManyToOne(fetch = FetchType.LAZY) // 多個 Portfolio 項目對應到一個 User
    @JoinColumn(name = "user_id", nullable = false) // 外鍵欄位名稱
    private User user;
    // ---------------

    @Column(name = "stock_symbol", nullable = false)
    private String stockSymbol; // 股票代號

    @Column(nullable = false)
    private Double quantity; // 持有數量

    @Column(name = "purchase_price", nullable = false)
    private Double purchasePrice; // 買入價格

    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now(); // 設定為當下時間
    }
}