package com.eleks.groupservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class PaymentResponseDto extends PaymentRequestDto {

    private Long id;
    private Long creatorId;
    private Long groupId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss'T'dd-MM-yyyy", timezone = "UTC")
    private Instant timestamp;
}