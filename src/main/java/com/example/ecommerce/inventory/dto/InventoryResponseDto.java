package com.example.ecommerce.inventory.dto;

public record InventoryResponseDto(
        Long id,
        Long productId,
        int availableQuantity,
        int reservedQuantity,
        long version
) {
}
