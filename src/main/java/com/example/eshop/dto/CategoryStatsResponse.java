package com.example.eshop.dto;

import java.math.BigDecimal;

public record CategoryStatsResponse(
        Long categoryId,
        String categoryName,
        Long productCount,
        BigDecimal inventoryValue
) {
    // Constructor dự phòng để hứng kiểu int/Number từ Hibernate rồi convert sang BigDecimal
    public CategoryStatsResponse(Long categoryId, String categoryName, Long productCount, Number inventoryValue) {
        this(
                categoryId,
                categoryName,
                productCount,
                inventoryValue == null ? BigDecimal.ZERO : new BigDecimal(inventoryValue.toString())
        );
    }
}