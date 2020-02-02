package com.eleks.groupservice.dto;

import com.eleks.groupservice.domain.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class GroupRequestDto {

    @NotNull(message = "groupName is required")
    @Size(min = 1, max = 50, message = "groupName length should be between 1 and 50")
    private String groupName;

    @NotNull(message = "currency is required")
    private Currency currency;

    @NotNull(message = "members is required")
    private List<Long> members;
}
