package com.example.ecommerce.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PlaceOrderRequestDto(
        @NotNull Long userId,
        @Size(max = 120) String idempotencyKey,
        @NotEmpty @Size(max = 100) List<@Valid OrderItemRequestDto> items
) {
}
