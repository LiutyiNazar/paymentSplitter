package com.eleks.groupservice.service.impl;

import com.eleks.groupservice.domain.Payment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PaymentsCalculator {

    public static Map<Long, Double> calculateValues(Long requesterId, List<Payment> payments, List<Long> otherMembersIds) {
        Map<Long, Double> values = new HashMap<>();
        for (Long memberId : otherMembersIds) {
            values.put(memberId, 0D);
            for (Payment payment : payments) {
                Double value = calculateValueForUserPerPayment(requesterId, memberId, payment);
                values.put(memberId, values.get(memberId) + value);
            }
        }
        return values;
    }

    private static Double calculateValueForUserPerPayment(Long requesterId, Long memberId, Payment payment) {
        List<Long> coPayers = payment.getCoPayers();
        if (coPayers.contains(requesterId) && coPayers.contains(memberId)) {
            double slice = payment.getPrice() / payment.getCoPayers().size();
            if (payment.getCreatorId().equals(requesterId)) {
                return slice;
            } else if (payment.getCreatorId().equals(memberId)) {
                return -slice;
            } else {
                return 0D;
            }
        } else {
            return 0D;
        }
    }
}
