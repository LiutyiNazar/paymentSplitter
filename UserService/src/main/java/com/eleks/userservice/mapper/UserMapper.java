package com.eleks.userservice.mapper;

import com.eleks.userservice.domain.User;
import com.eleks.userservice.dto.user.UserRequestDto;
import com.eleks.userservice.dto.user.UserResponseDto;

import java.util.Objects;

public class UserMapper {

    public static User toEntity(UserRequestDto dto) {
        if (Objects.isNull(dto)) {
            return null;
        } else {
            return User.builder()
                    .username(dto.getUsername())
                    .password(dto.getPassword())
                    .lastName(dto.getLastName())
                    .firstName(dto.getFirstName())
                    .email(dto.getEmail())
                    .receiveNotifications(dto.getReceiveNotifications())
                    .dateOfBirth(dto.getDateOfBirth())
                    .build();
        }
    }

    public static UserResponseDto toDto(User entity) {
        if (Objects.isNull(entity)) {
            return null;
        } else {
            return UserResponseDto.builder()
                    .id(entity.getId())
                    .username(entity.getUsername())
                    .lastName(entity.getLastName())
                    .firstName(entity.getFirstName())
                    .email(entity.getEmail())
                    .receiveNotifications(entity.getReceiveNotifications())
                    .dateOfBirth(entity.getDateOfBirth())
                    .build();
        }
    }
}
