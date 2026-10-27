package com.example.eshop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer quantity,
        String imageUrl,
        Long categoryId,
        String categoryName,
        LocalDateTime createdAt
) {}