package com.eleks.groupservice.dto;

import com.eleks.groupservice.domain.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
// TODO rename to StatusResponseDto
public class UserStatusDto {
    private Long userId;
    private String username;
    private Currency currency;
    private Double value;
}
