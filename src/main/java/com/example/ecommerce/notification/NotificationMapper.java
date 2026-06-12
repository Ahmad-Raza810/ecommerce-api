package com.example.ecommerce.notification;

import com.example.ecommerce.notification.dto.NotificationResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    @Mapping(target = "userId", source = "user.id")
    NotificationResponseDto toResponse(Notification notification);
}
