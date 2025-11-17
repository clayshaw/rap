package com.example.demo.repository;

import com.example.demo.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // 標記這是一個 Spring 的 Repository 元件
public interface UserRepository extends JpaRepository<User, Long> {

    // 只需要這樣定義，Spring Data JPA 就會自動幫你實作：
    // "SELECT * FROM users WHERE username = ?"
    Optional<User> findByUsername(String username);

    // "SELECT * FROM users WHERE email = ?"
    Optional<User> findByEmail(String email);
}