package com.example.demo.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice // 告訴 Spring 這是全域例外處理器
public class GlobalExceptionHandler {

    /**
     * 專門處理我們在 Service 中手動拋出的 RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException ex) {

        // 1. (*** 關鍵 ***)
        //    我們建立一個 JSON 物件 (Map)，只包含 "message" 欄位
        Map<String, String> errorResponse = Map.of(
            "message", ex.getMessage() // 取得 "Error: Username is already taken!"
        );

        // 2. 回傳 400 Bad Request，並在 body 中放入 JSON
        // (400 比 500 更適合用於 "使用者輸入錯誤" 的情境)
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}