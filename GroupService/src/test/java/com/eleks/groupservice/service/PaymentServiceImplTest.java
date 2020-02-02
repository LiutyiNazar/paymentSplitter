package com.eleks.groupservice.service;

import com.eleks.groupservice.domain.Currency;
import com.eleks.groupservice.domain.Group;
import com.eleks.groupservice.domain.Payment;
import com.eleks.groupservice.dto.PaymentRequestDto;
import com.eleks.groupservice.dto.PaymentResponseDto;
import com.eleks.groupservice.exception.ResourceNotFoundException;
import com.eleks.groupservice.exception.UsersIdsValidationException;
import com.eleks.groupservice.repository.GroupRepository;
import com.eleks.groupservice.repository.PaymentRepository;
import com.eleks.groupservice.service.impl.PaymentServiceImpl;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    GroupRepository groupRepo;

    @Mock
    PaymentRepository paymentRepo;

    @InjectMocks
    PaymentServiceImpl service;

    PaymentRequestDto paymentRequest;

    Group group;

    Payment payment;

    Long creatorId = 1L;

    @BeforeEach
    void setUp() {
        paymentRequest = PaymentRequestDto.builder()
                .paymentDescription("paymentDescription")
                .coPayers(Lists.newArrayList(1L, 2L))
                .price(100D)
                .build();

        group = Group.builder()
                .id(1L)
                .members(Lists.newArrayList(1L, 2L))
                .currency(Currency.UAH)
                .groupName("groupName")
                .build();

        payment = Payment.builder()
                .id(1L)
                .creatorId(creatorId)
                .group(group)
                .price(paymentRequest.getPrice())
                .coPayers(paymentRequest.getCoPayers())
                .paymentDescription(paymentRequest.getPaymentDescription())
                .timestamp(Instant.now())
                .build();
    }

    @Test
    void createPayment_GroupExistsCoPayersAreValid_SaveAndReturnResponseDto() {
        when(groupRepo.findById(group.getId())).thenReturn(Optional.of(group));

        when(paymentRepo.save(any(Payment.class))).thenReturn(payment);

        PaymentResponseDto response = service.createPayment(group.getId(), creatorId, paymentRequest);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertNotNull(response.getGroupId());
        assertNotNull(response.getCreatorId());
        assertNotNull(response.getTimestamp());
        assertEquals(paymentRequest.getPaymentDescription(), response.getPaymentDescription());
        assertEquals(paymentRequest.getPrice(), response.getPrice());
        assertEquals(paymentRequest.getCoPayers(), response.getCoPayers());
    }

    @Test
    void createPayment_GroupDoesntExist_ThrowResourceNotFoundException() {
        when(groupRepo.findById(group.getId())).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> service.createPayment(group.getId(), creatorId, paymentRequest));

        assertEquals("Group doesn't exist", ex.getMessage());
    }

    @Test
    void createPayment_CoPayersIdsNotFromRequestedGroup_ThrowUsersIdsValidationException() {
        when(groupRepo.findById(group.getId())).thenReturn(Optional.of(group));
        paymentRequest.setCoPayers(Lists.newArrayList(42L, 24L));

        UsersIdsValidationException ex = assertThrows(UsersIdsValidationException.class,
                () -> service.createPayment(group.getId(), creatorId, paymentRequest));

        assertEquals("Payment co-payers are not members of group", ex.getMessage());
    }

    @Test
    void getPayment_GroupAndPaymentExists_ShouldReturnResponseModel() {
        group.getPayments().add(payment);

        when(groupRepo.findById(group.getId())).thenReturn(Optional.of(group));

        Optional<PaymentResponseDto> result = service.getPayment(group.getId(), payment.getId());

        assertTrue(result.isPresent());
    }

    @Test
    void getPayment_GroupDoesntExist_ShouldReturnEmptyOptional() {
        when(groupRepo.findById(group.getId())).thenReturn(Optional.empty());

        Optional<PaymentResponseDto> result = service.getPayment(group.getId(), payment.getId());

        assertFalse(result.isPresent());
    }

    @Test
    void getPayment_GroupExistsButPaymentIsNot_ShouldReturnEmptyOptional() {
        group.setPayments(Collections.emptyList());

        when(groupRepo.findById(group.getId())).thenReturn(Optional.of(group));

        Optional<PaymentResponseDto> result = service.getPayment(group.getId(), payment.getId());

        assertFalse(result.isPresent());
    }

    @Test
    void getPayments_GroupExistAndHasPayments_ReturnListOfPayments() {
        group.getPayments().add(payment);

        when(groupRepo.findById(group.getId())).thenReturn(Optional.of(group));

        Optional<List<PaymentResponseDto>> result = service.getPayments(group.getId());

        assertTrue(result.isPresent());
        assertEquals(group.getPayments().get(0).getId(), result.get().get(0).getId());
    }

    @Test
    void getPayments_GroupExistButWithoutPayments_ReturnEmptyList() {
        group.setPayments(Collections.emptyList());

        when(groupRepo.findById(group.getId())).thenReturn(Optional.of(group));

        Optional<List<PaymentResponseDto>> result = service.getPayments(group.getId());

        assertTrue(result.isPresent());
        assertTrue(result.get().isEmpty());
    }

    @Test
    void getPayments_GroupDoesntExist_ReturnEmptyOptional() {
        when(groupRepo.findById(group.getId())).thenReturn(Optional.empty());

        Optional<List<PaymentResponseDto>> result = service.getPayments(group.getId());

        assertFalse(result.isPresent());
    }

    @Test
    void deletePayment_GroupAndPaymentExist_ShouldCallDelete() {
        payment.setGroup(group);

        when(paymentRepo.findById(payment.getId())).thenReturn(Optional.of(payment));

        service.deletePayment(group.getId(), payment.getId());

        verify(paymentRepo).deleteById(payment.getId());
    }

    @Test
    void deletePayment_PaymentDoesntExist_ShouldThrowResourceNotFoundException() {
        when(paymentRepo.findById(payment.getId())).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> service.deletePayment(group.getId(), payment.getId()));

        assertEquals("Payment doesn't exists", ex.getMessage());
    }

    @Test
    void deletePayment_PaymentExistButItBelongsToAnotherGroup_ShouldThrowResourceNotFoundException() {
        payment.setGroup(group);

        when(paymentRepo.findById(payment.getId())).thenReturn(Optional.of(payment));

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> service.deletePayment(222L, payment.getId()));

        assertEquals("Payment doesn't exists", ex.getMessage());
    }

}