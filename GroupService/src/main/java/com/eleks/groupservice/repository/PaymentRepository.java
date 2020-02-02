package com.eleks.groupservice.repository;

import com.eleks.groupservice.domain.Group;
import com.eleks.groupservice.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
