package com.example.ecommerce.auth.dto;

import com.example.ecommerce.user.dto.UserResponseDto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        UserResponseDto user
) {
}
