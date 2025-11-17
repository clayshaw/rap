package com.example.demo.dto;

// 使用 'record' 可以讓我們用一行程式碼
// 就定義好一個 'class'，它會自動包含
// 'username' 和 'password' 欄位，以及它們的建構子
public record LoginRequest(String username, String password) {

    
}