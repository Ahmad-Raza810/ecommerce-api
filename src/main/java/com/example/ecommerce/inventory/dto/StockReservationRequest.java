package com.example.ecommerce.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StockReservationRequest(
        @NotNull Long productId,
        @Positive int quantity
) {
}
