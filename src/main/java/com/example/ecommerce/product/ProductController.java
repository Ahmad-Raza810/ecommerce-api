package com.example.ecommerce.product;

import com.example.ecommerce.common.ApiResponse;
import com.example.ecommerce.common.ResponseFactory;
import com.example.ecommerce.product.dto.ProductRequestDto;
import com.example.ecommerce.product.dto.ProductResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @Operation(summary = "Create product")
    ResponseEntity<ApiResponse<ProductResponseDto>> create(@Valid @RequestBody ProductRequestDto request) {
        return ResponseFactory.created("Product created successfully", productService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product")
    ResponseEntity<ApiResponse<ProductResponseDto>> update(@PathVariable Long id,
                                                           @Valid @RequestBody ProductRequestDto request) {
        return ResponseFactory.ok("Product updated successfully", productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate product")
    ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseFactory.ok("Product deleted successfully", null);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product")
    ResponseEntity<ApiResponse<ProductResponseDto>> get(@PathVariable Long id) {
        return ResponseFactory.ok("Product fetched successfully", productService.get(id));
    }

    @GetMapping
    @Operation(summary = "List products")
    ResponseEntity<ApiResponse<List<ProductResponseDto>>> list() {
        return ResponseFactory.ok("Products fetched successfully", productService.list());
    }
}
