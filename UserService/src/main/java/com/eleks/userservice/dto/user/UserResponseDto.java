package com.eleks.userservice.dto.user;

import com.eleks.userservice.serializer.SimpleDateJsonDeserializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = SimpleDateJsonDeserializer.PATTERN)
    private LocalDate dateOfBirth;
    private String email;
    private Boolean receiveNotifications;
}
