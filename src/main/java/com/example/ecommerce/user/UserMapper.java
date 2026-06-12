package com.example.ecommerce.user;

import com.example.ecommerce.user.dto.UserRequestDto;
import com.example.ecommerce.user.dto.UserResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    User toEntity(UserRequestDto dto);

    UserResponseDto toResponse(User user);
}
