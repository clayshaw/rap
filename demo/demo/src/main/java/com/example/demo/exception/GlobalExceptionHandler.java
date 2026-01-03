package com.example.demo.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException; // [新增]
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 當 UserService 拋出 BadCredentialsException 時會進入這裡
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentialsException(BadCredentialsException ex) {
        Map<String, String> errorResponse = Map.of(
            "message", ex.getMessage() // 例如 "Error: Invalid password."
        );
        // 回傳 401 狀態碼
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * 例如註冊時的使用者名稱重複等邏輯錯誤
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException ex) {

        String errorMessage = ex.getMessage() != null ? ex.getMessage() : "An unexpected application error occurred.";
        Map<String, String> errorResponse = Map.of(
            "message", errorMessage
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}