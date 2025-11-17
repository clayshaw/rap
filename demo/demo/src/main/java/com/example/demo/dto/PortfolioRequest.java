package com.example.demo.dto;

// 匯入驗證註解
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// 我們使用 Java 'record' 來快速建立這個 DTO
public record PortfolioRequest(

    @NotBlank(message = "股票代號不可為空")
    String stockSymbol,

    @NotNull(message = "數量不可為空")
    @Positive(message = "數量必須大於 0")
    Double quantity,

    @NotNull(message = "價格不可為空")
    @Positive(message = "價格必須大於 0")
    Double purchasePrice
) {
}