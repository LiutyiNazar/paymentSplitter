package com.eleks.groupservice.controller;

import com.eleks.common.security.SecurityPrincipalHolder;
import com.eleks.groupservice.dto.PaymentRequestDto;
import com.eleks.groupservice.dto.PaymentResponseDto;
import com.eleks.groupservice.exception.ResourceNotFoundException;
import com.eleks.groupservice.service.PaymentService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@Api(value = "payment", description = "Payment API")
public class PaymentController {
    private PaymentService service;
    private SecurityPrincipalHolder principalHolder;

    @Autowired
    public PaymentController(PaymentService service, SecurityPrincipalHolder principalHolder) {
        this.service = service;
        this.principalHolder = principalHolder;
    }

    @PostMapping("/groups/{groupId}/payments")
    public PaymentResponseDto createPayment(@PathVariable Long groupId, @Valid @RequestBody PaymentRequestDto requestDto) {
        Long loggedUserId = principalHolder.getPrincipal().getUserId();
        return service.createPayment(groupId, loggedUserId, requestDto);
    }

    @GetMapping("/groups/{groupId}/payments/{paymentId}")
    public PaymentResponseDto getPayment(@PathVariable Long groupId, @PathVariable Long paymentId) {
        return service.getPayment(groupId, paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("payment does't exist"));
    }

    @GetMapping("/groups/{groupId}/payments")
    public List<PaymentResponseDto> getPayments(@PathVariable Long groupId) {
        return service.getPayments(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("group does't exist"));
    }

    @DeleteMapping("/groups/{groupId}/payments/{paymentId}")
    public void deletePayment(@PathVariable Long groupId, @PathVariable Long paymentId) {
        service.deletePayment(groupId, paymentId);
    }
}
