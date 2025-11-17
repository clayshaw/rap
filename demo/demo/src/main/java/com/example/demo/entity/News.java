package com.example.demo.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity; 
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table; 
import lombok.Data; 

@Data 
@Entity 
@Table(name = "news") 
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String stockSymbol; // 股票代號

    @Column(nullable = false, unique = true)
    private String title; // 新聞標題

    //    加上 "unique = true"
    //    這可以防止 Python 爬蟲寫入重複的網址
    @Column(nullable = false, length = 1000, unique = true)
    private String url; // 新聞原文網址
    
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;


}