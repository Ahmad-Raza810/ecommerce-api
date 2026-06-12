package com.example.ecommerce.inventory;

import com.example.ecommerce.common.ApiResponse;
import com.example.ecommerce.common.ResponseFactory;
import com.example.ecommerce.inventory.dto.InventoryRequestDto;
import com.example.ecommerce.inventory.dto.InventoryResponseDto;
import com.example.ecommerce.inventory.dto.StockAdjustmentRequest;
import com.example.ecommerce.inventory.dto.StockReservationRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    @Operation(summary = "Create inventory")
    ResponseEntity<ApiResponse<InventoryResponseDto>> create(@Valid @RequestBody InventoryRequestDto request) {
        return ResponseFactory.created("Inventory created successfully", inventoryService.create(request));
    }

    @PutMapping("/products/{productId}/stock")
    @Operation(summary = "Update stock")
    ResponseEntity<ApiResponse<InventoryResponseDto>> updateStock(@PathVariable Long productId,
                                                                  @Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseFactory.ok("Stock updated successfully", inventoryService.updateStock(productId, request));
    }

    @PostMapping("/reserve")
    @Operation(summary = "Reserve stock")
    ResponseEntity<ApiResponse<Void>> reserve(@Valid @RequestBody StockReservationRequest request) {
        inventoryService.reserveStock(request.productId(), request.quantity());
        return ResponseFactory.ok("Stock reserved successfully", null);
    }

    @PostMapping("/release")
    @Operation(summary = "Release stock")
    ResponseEntity<ApiResponse<Void>> release(@Valid @RequestBody StockReservationRequest request) {
        inventoryService.releaseStock(request.productId(), request.quantity());
        return ResponseFactory.ok("Stock released successfully", null);
    }

    @GetMapping("/products/{productId}")
    @Operation(summary = "Get inventory by product")
    ResponseEntity<ApiResponse<InventoryResponseDto>> getByProduct(@PathVariable Long productId) {
        return ResponseFactory.ok("Inventory fetched successfully", inventoryService.getByProduct(productId));
    }
}
