package com.eleks.groupservice.service;

import com.eleks.groupservice.dto.PaymentRequestDto;
import com.eleks.groupservice.dto.PaymentResponseDto;
import com.eleks.groupservice.exception.ResourceNotFoundException;
import com.eleks.groupservice.exception.UsersIdsValidationException;

import java.util.List;
import java.util.Optional;

public interface PaymentService {
    void deletePayment(Long groupId, Long paymentId) throws ResourceNotFoundException;

    Optional<PaymentResponseDto> getPayment(Long groupId, Long paymentId);

    Optional<List<PaymentResponseDto>> getPayments(Long groupId);

    PaymentResponseDto createPayment(Long groupId, Long creatorId, PaymentRequestDto requestDto) throws ResourceNotFoundException, UsersIdsValidationException;
}
