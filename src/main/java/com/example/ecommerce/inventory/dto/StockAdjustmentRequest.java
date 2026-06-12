package com.example.ecommerce.inventory.dto;

import jakarta.validation.constraints.Min;

public record StockAdjustmentRequest(@Min(0) int availableQuantity) {
}
