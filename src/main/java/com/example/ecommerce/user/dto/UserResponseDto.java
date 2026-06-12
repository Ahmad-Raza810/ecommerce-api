package com.example.ecommerce.user.dto;

import com.example.ecommerce.user.Role;

import java.time.Instant;

public record UserResponseDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        Role role,
        Instant createdAt,
        Instant updatedAt
) {
}
