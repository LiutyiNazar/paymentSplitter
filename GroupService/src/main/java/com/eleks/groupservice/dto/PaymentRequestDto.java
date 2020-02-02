package com.eleks.groupservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Set;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDto {
    @NotNull(message = "paymentDescription is required")
    @Size(min = 1, max = 200, message = "paymentDescription length should be between 1 and 200")
    private String paymentDescription;

    @NotNull(message = "price is required")
    @Positive(message = "price can't be negative or zero")
    private Double price;

    @NotNull(message = "coPayers is required")
    private List<Long> coPayers;
}
