package com.eleks.groupservice.service;

import com.eleks.groupservice.domain.Payment;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentsCalculationHelperTest {

    @Test
    public void calculateValues_NoOtherMembers_ReturnEmptyValuesMap() {
        List<Payment> payments = Collections.singletonList(Payment.builder().build());
        Map<Long, Double> values = PaymentsCalculationHelper.calculateValues(1L, payments, Collections.emptyList());

        assertTrue(values.isEmpty());
    }

    @Test
    public void calculateValues_NoPayments_ReturnValuesMapWithOnlyZeros() {
        Map<Long, Double> values = PaymentsCalculationHelper.calculateValues(1L, Collections.emptyList(), Arrays.asList(1L, 2L));

        for (Double value : values.values()) {
            assertEquals(0D, value);
        }
    }

    @Test
    public void calculateValues_NoPaymentsWithRequester_ReturnValuesMapWithOnlyZeros() {
        List<Long> otherMembers = Arrays.asList(2L, 3L);
        List<Payment> payments = Arrays.asList(
                Payment.builder()
                        .creatorId(2L)
                        .coPayers(Lists.newArrayList(2L, 3L))
                        .price(100D)
                        .build(),
                Payment.builder()
                        .creatorId(3L)
                        .coPayers(Lists.newArrayList(2L, 3L))
                        .price(50D)
                        .build()
        );

        Map<Long, Double> values = PaymentsCalculationHelper.calculateValues(1L, payments, otherMembers);

        for (Double value : values.values()) {
            assertEquals(0D, value);
        }
    }

    @Test
    public void calculateValues_OnePaymentByRequester_ReturnPositiveValueForTwoOtherMembers() {
        List<Long> otherMembers = Arrays.asList(2L, 3L);
        List<Payment> payments = Collections.singletonList(Payment.builder()
                .creatorId(1L)
                .coPayers(Lists.newArrayList(1L, 2L, 3L))
                .price(120.12)
                .build());

        Map<Long, Double> values = PaymentsCalculationHelper.calculateValues(1L, payments, otherMembers);

        assertEquals(2, values.size());
        assertEquals(40.04, values.get(2L));
        assertEquals(40.04, values.get(3L));
    }

    @Test
    public void calculateValues_OnePaymentByNonRequester_ReturnNegativeValueForOneOfOtherMembers() {
        List<Long> otherMembers = Arrays.asList(2L, 3L);
        List<Payment> payments = Collections.singletonList(Payment.builder()
                .creatorId(2L)
                .coPayers(Lists.newArrayList(1L, 2L, 3L))
                .price(120.12)
                .build());

        Map<Long, Double> values = PaymentsCalculationHelper.calculateValues(1L, payments, otherMembers);

        assertEquals(2, values.size());
        assertEquals(-40.04, values.get(2L));
        assertEquals(0D, values.get(3L));
    }

    @Test
    public void calculateValues_TwoPaymentsByRequester_ReturnPositiveSumValuesForOtherMembers() {
        List<Long> otherMembers = Arrays.asList(2L, 3L);
        List<Payment> payments = Arrays.asList(
                Payment.builder()
                        .creatorId(1L)
                        .coPayers(Lists.newArrayList(1L, 2L))
                        .price(120D)
                        .build(),
                Payment.builder()
                        .creatorId(1L)
                        .coPayers(Lists.newArrayList(1L, 2L, 3L))
                        .price(60D)
                        .build()
        );

        Map<Long, Double> values = PaymentsCalculationHelper.calculateValues(1L, payments, otherMembers);

        assertEquals(2, values.size());
        assertEquals(80D, values.get(2L));
        assertEquals(20D, values.get(3L));
    }

    @Test
    public void calculateValues_TwoPaymentsByNonRequester_ReturnNegativeSumValueForOneOfOtherMembers() {
        List<Long> otherMembers = Arrays.asList(2L, 3L);
        List<Payment> payments = Arrays.asList(
                Payment.builder()
                        .creatorId(2L)
                        .coPayers(Lists.newArrayList(1L, 2L))
                        .price(120D)
                        .build(),
                Payment.builder()
                        .creatorId(2L)
                        .coPayers(Lists.newArrayList(1L, 2L, 3L))
                        .price(60D)
                        .build()
        );

        Map<Long, Double> values = PaymentsCalculationHelper.calculateValues(1L, payments, otherMembers);

        assertEquals(2, values.size());
        assertEquals(-80D, values.get(2L));
        assertEquals(0D, values.get(3L));
    }

    @Test
    public void calculateValues_OnePaymentByRequesterAndOneNot_ReturnRightValuesForOtherMembers() {
        List<Long> otherMembers = Arrays.asList(2L, 3L);
        List<Payment> payments = Arrays.asList(
                Payment.builder()
                        .creatorId(2L)
                        .coPayers(Lists.newArrayList(1L, 2L))
                        .price(120D)
                        .build(),
                Payment.builder()
                        .creatorId(1L)
                        .coPayers(Lists.newArrayList(1L, 2L, 3L))
                        .price(60D)
                        .build()
        );

        Map<Long, Double> values = PaymentsCalculationHelper.calculateValues(1L, payments, otherMembers);

        assertEquals(2, values.size());
        assertEquals(-40D, values.get(2L));
        assertEquals(20D, values.get(3L));
    }
}