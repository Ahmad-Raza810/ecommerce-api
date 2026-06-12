package com.example.ecommerce.inventory;

import com.example.ecommerce.inventory.dto.InventoryResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryMapper {
    @Mapping(target = "productId", source = "product.id")
    InventoryResponseDto toResponse(Inventory inventory);
}
