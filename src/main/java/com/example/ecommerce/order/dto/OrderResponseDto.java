package com.example.ecommerce.order.dto;

import com.example.ecommerce.order.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponseDto(
        Long id,
        Long userId,
        OrderStatus status,
        BigDecimal totalAmount,
        String idempotencyKey,
        List<OrderItemResponseDto> items,
        Instant createdAt,
        Instant updatedAt
) {
}
