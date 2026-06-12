package com.example.ecommerce.notification.dto;

import com.example.ecommerce.notification.NotificationStatus;
import com.example.ecommerce.notification.NotificationType;

import java.time.Instant;

public record NotificationResponseDto(
        Long id,
        Long userId,
        String message,
        NotificationType type,
        NotificationStatus status,
        Instant createdAt
) {
}
