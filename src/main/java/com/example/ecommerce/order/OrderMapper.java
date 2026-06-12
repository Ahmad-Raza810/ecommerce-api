package com.example.ecommerce.order;

import com.example.ecommerce.order.dto.OrderItemResponseDto;
import com.example.ecommerce.order.dto.OrderResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "userId", source = "user.id")
    OrderResponseDto toResponse(CustomerOrder order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    OrderItemResponseDto toItemResponse(OrderItem item);
}
