package com.example.ecommerce.order;

import com.example.ecommerce.common.ApiResponse;
import com.example.ecommerce.common.ResponseFactory;
import com.example.ecommerce.order.dto.OrderResponseDto;
import com.example.ecommerce.order.dto.PlaceOrderRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Place order")
    ResponseEntity<ApiResponse<OrderResponseDto>> placeOrder(@Valid @RequestBody PlaceOrderRequestDto request) {
        return ResponseFactory.created("Order created successfully", orderService.placeOrder(request));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or @orderService.isOrderOwner(#id, authentication.name)")
    @Operation(summary = "Cancel order")
    ResponseEntity<ApiResponse<OrderResponseDto>> cancelOrder(@PathVariable Long id) {
        return ResponseFactory.ok("Order cancelled successfully", orderService.cancelOrder(id));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @orderService.isOrderOwner(#id, authentication.name)")
    @Operation(summary = "Get order")
    ResponseEntity<ApiResponse<OrderResponseDto>> getOrder(@PathVariable Long id) {
        return ResponseFactory.ok("Order fetched successfully", orderService.getOrder(id));
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN') or authentication.name == @userService.getEntity(#userId).email")
    @Operation(summary = "Get orders by user")
    ResponseEntity<ApiResponse<List<OrderResponseDto>>> getOrdersByUser(@PathVariable Long userId) {
        return ResponseFactory.ok("Orders fetched successfully", orderService.getOrdersByUser(userId));
    }
}
