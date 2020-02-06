package com.eleks.userservice.mapper;

import com.eleks.userservice.domain.User;
import com.eleks.userservice.dto.user.UserRequestDto;
import com.eleks.userservice.dto.user.UserResponseDto;

import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

public class UserMapper {

    public static User toEntity(UserRequestDto requestDto) {
        return ofNullable(requestDto)
                .map(dto -> User.builder()
                        .username(dto.getUsername())
                        .password(dto.getPassword())
                        .lastName(dto.getLastName())
                        .firstName(dto.getFirstName())
                        .email(dto.getEmail())
                        .receiveNotifications(dto.getReceiveNotifications())
                        .dateOfBirth(dto.getDateOfBirth())
                        .build())
                .orElse(null);
    }

    public static UserResponseDto toDto(User user) {
        return ofNullable(user)
                .map(entity -> UserResponseDto.builder()
                        .id(entity.getId())
                        .username(entity.getUsername())
                        .lastName(entity.getLastName())
                        .firstName(entity.getFirstName())
                        .email(entity.getEmail())
                        .receiveNotifications(entity.getReceiveNotifications())
                        .dateOfBirth(entity.getDateOfBirth())
                        .build())
                .orElse(null);
    }
}
