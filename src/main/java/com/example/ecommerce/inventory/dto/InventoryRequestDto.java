package com.example.ecommerce.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InventoryRequestDto(
        @NotNull Long productId,
        @Min(0) int availableQuantity,
        @Min(0) int reservedQuantity
) {
}
