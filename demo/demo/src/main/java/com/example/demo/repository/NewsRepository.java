package com.example.demo.repository;

import com.example.demo.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

    // 查詢 "在指定清單中 (In)" 的所有新聞，並依時間排序
    List<News> findByStockSymbolInOrderByCreatedAtDesc(List<String> symbols);
}