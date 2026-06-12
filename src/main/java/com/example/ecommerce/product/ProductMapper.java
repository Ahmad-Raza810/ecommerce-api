package com.example.ecommerce.product;

import com.example.ecommerce.product.dto.ProductRequestDto;
import com.example.ecommerce.product.dto.ProductResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "id", ignore = true)
    Product toEntity(ProductRequestDto dto);

    ProductResponseDto toResponse(Product product);

    @Mapping(target = "id", ignore = true)
    void updateEntity(ProductRequestDto dto, @MappingTarget Product product);
}
