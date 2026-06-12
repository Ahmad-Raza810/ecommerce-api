package com.example.ecommerce.product.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponseDto(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String category,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
