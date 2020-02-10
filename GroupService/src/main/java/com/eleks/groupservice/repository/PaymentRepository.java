package com.eleks.groupservice.repository;

import com.eleks.groupservice.domain.Group;
import com.eleks.groupservice.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
