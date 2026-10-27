package com.example.eshop.dto;

import jakarta.validation.constraints.NotNull;

public record StockRequest(
        @NotNull(message = "Delta không được để trống")
        Integer delta
) {}