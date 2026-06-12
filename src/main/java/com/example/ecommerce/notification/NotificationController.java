package com.example.ecommerce.notification;

import com.example.ecommerce.common.ApiResponse;
import com.example.ecommerce.common.ResponseFactory;
import com.example.ecommerce.notification.dto.NotificationResponseDto;
import com.example.ecommerce.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN') or authentication.name == @userService.getEntity(#userId).email")
    @Operation(summary = "Get notifications by user")
    ResponseEntity<ApiResponse<List<NotificationResponseDto>>> listForUser(@PathVariable Long userId) {
        return ResponseFactory.ok("Notifications fetched successfully",
                notificationService.listForUser(userId, userService));
    }
}
