package com.eleks.groupservice.mapper;

import com.eleks.groupservice.domain.Group;
import com.eleks.groupservice.domain.Payment;
import com.eleks.groupservice.dto.PaymentRequestDto;
import com.eleks.groupservice.dto.PaymentResponseDto;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class PaymentMapperTest {

    @Test
    public void toEntity() {
        Long creatorId = 1L;
        Group group = Group.builder()
                .id(2L)
                .build();
        PaymentRequestDto dto = PaymentRequestDto.builder()
                .paymentDescription("paymentDescription")
                .coPayers(Lists.newArrayList(1L, 2L, 3L))
                .price(100.5)
                .build();

        Payment entity = PaymentMapper.toEntity(creatorId, group, dto);

        assertNotNull(entity);
        assertNull(entity.getId());
        assertNull(entity.getTimestamp());
        assertEquals(dto.getPaymentDescription(), entity.getPaymentDescription());
        assertEquals(dto.getCoPayers(), entity.getCoPayers());
        assertEquals(dto.getPrice(), entity.getPrice());
        assertEquals(group.getId(), entity.getGroup().getId());
    }

    @Test
    public void toDto() {
        Group group = Group.builder()
                .id(2L)
                .build();

        Payment entity = Payment.builder()
                .id(1L)
                .creatorId(2L)
                .group(group)
                .paymentDescription("paymentDescription")
                .price(200.5)
                .coPayers(Lists.newArrayList(1L, 2L, 3L))
                .timestamp(Instant.now())
                .build();

        PaymentResponseDto dto = PaymentMapper.toDto(entity);

        assertNotNull(dto);
        assertEquals(entity.getId(), dto.getId());
        assertEquals(entity.getCreatorId(), dto.getCreatorId());
        assertEquals(entity.getGroup().getId(), dto.getGroupId());
        assertEquals(entity.getPaymentDescription(), dto.getPaymentDescription());
        assertEquals(entity.getCoPayers(), dto.getCoPayers());
        assertEquals(entity.getPrice(), dto.getPrice());
        assertEquals(entity.getTimestamp(), dto.getTimestamp());
    }
}