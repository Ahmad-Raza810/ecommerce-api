package com.example.ecommerce.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequestDto(
        @NotBlank @Size(max = 180) String name,
        @Size(max = 5000) String description,
        @NotNull @Positive BigDecimal price,
        @NotBlank @Size(max = 100) String category,
        boolean active
) {
}
