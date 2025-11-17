// 位置: src/main/java/com/example/demo/repository/PortfolioRepository.java
package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Portfolio;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    // 自動產生: "SELECT * FROM portfolios WHERE user_id = ?"
    List<Portfolio> findByUserIdOrderByCreatedAtDesc(Long userId);

}