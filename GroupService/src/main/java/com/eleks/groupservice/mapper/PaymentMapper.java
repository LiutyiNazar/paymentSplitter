package com.eleks.groupservice.mapper;

import com.eleks.groupservice.domain.Group;
import com.eleks.groupservice.domain.Payment;
import com.eleks.groupservice.dto.PaymentRequestDto;
import com.eleks.groupservice.dto.PaymentResponseDto;

import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

public class PaymentMapper {

    public static Payment toEntity(Long creatorId, Group group, PaymentRequestDto requestDto) {
        return ofNullable(requestDto)
                .map(dto -> Payment.builder()
                        .paymentDescription(dto.getPaymentDescription())
                        .price(dto.getPrice())
                        .coPayers(dto.getCoPayers())
                        .creatorId(creatorId)
                        .group(group)
                        .build())
                .orElse(null);
    }

    public static PaymentResponseDto toDto(Payment payment) {
        return ofNullable(payment)
                .map(entity -> PaymentResponseDto.builder()
                        .id(entity.getId())
                        .paymentDescription(entity.getPaymentDescription())
                        .price(entity.getPrice())
                        .coPayers(entity.getCoPayers())
                        .creatorId(entity.getCreatorId())
                        .groupId(entity.getGroup().getId())
                        .timestamp(entity.getTimestamp())
                        .build())
                .orElse(null);
    }
}
