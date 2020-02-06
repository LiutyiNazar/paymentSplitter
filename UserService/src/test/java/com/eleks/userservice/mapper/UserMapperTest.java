package com.eleks.userservice.mapper;

import com.eleks.userservice.domain.User;
import com.eleks.userservice.dto.user.UserRequestDto;
import com.eleks.userservice.dto.user.UserResponseDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserMapperTest {

    @Test
    public void toEntity() {
        UserRequestDto request = UserRequestDto.builder()
                .username("PaulMcX")
                .firstName("Paul")
                .lastName("Liutyi")
                .dateOfBirth(LocalDate.now())
                .email("p.liutyi@eleks.com")
                .receiveNotifications(true)
                .build();

        User user = UserMapper.toEntity(request);

        assertNull(user.getId());
        assertEquals(request.getUsername(), user.getUsername());
        assertEquals(request.getFirstName(), user.getFirstName());
        assertEquals(request.getLastName(), user.getLastName());
        assertEquals(request.getEmail(), user.getEmail());
        assertEquals(request.getReceiveNotifications(), user.getReceiveNotifications());
        assertEquals(request.getDateOfBirth(), user.getDateOfBirth());
    }

    @Test
    public void toDto() {
        User user = User.builder()
                .id(1L)
                .username("PaulMcX")
                .firstName("Paul")
                .lastName("Liutyi")
                .dateOfBirth(LocalDate.now())
                .email("p.liutyi@eleks.com")
                .receiveNotifications(true)
                .build();

        UserResponseDto userResponseDto = UserMapper.toDto(user);

        assertEquals(user.getId(), userResponseDto.getId());
        assertEquals(user.getUsername(), userResponseDto.getUsername());
        assertEquals(user.getFirstName(), userResponseDto.getFirstName());
        assertEquals(user.getLastName(), userResponseDto.getLastName());
        assertEquals(user.getEmail(), userResponseDto.getEmail());
        assertEquals(user.getReceiveNotifications(), userResponseDto.getReceiveNotifications());
        assertEquals(user.getDateOfBirth(), userResponseDto.getDateOfBirth());
    }
}