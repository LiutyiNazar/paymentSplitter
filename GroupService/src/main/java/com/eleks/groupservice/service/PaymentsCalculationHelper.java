package com.eleks.groupservice.service;

import com.eleks.groupservice.domain.Payment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PaymentsCalculationHelper {

    public static Map<Long, Double> calculateValues(Long requesterId, List<Payment> payments, List<Long> otherMembersIds) {
        Map<Long, Double> values = new HashMap<>();
        otherMembersIds.forEach(memberId -> {
            values.put(memberId, 0D);
            payments.forEach(payment -> {
                Double value = calculateValueForUserPerPayment(requesterId, memberId, payment);
                values.put(memberId, values.get(memberId) + value);
            });
        });
        return values;
    }

    private static Double calculateValueForUserPerPayment(Long requesterId, Long memberId, Payment payment) {
        List<Long> coPayers = payment.getCoPayers();
        if (coPayers.contains(requesterId) && coPayers.contains(memberId)) {
            double bill = payment.getPrice() / payment.getCoPayers().size();
            if (payment.getCreatorId().equals(requesterId)) {
                return bill;
            } else if (payment.getCreatorId().equals(memberId)) {
                return -bill;
            }
        }
        return 0D;
    }
}
