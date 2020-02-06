package com.eleks.groupservice.repository;

import com.eleks.groupservice.domain.Currency;
import com.eleks.groupservice.domain.Group;
import com.eleks.groupservice.domain.Payment;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class PaymentRepositoryTest {

    @Autowired
    PaymentRepository repository;

    @Autowired
    TestEntityManager entityManager;

    Group group;
    Payment payment;

    @BeforeEach
    void setUp() {
        group = Group.builder()
                .id(1L)
                .groupName("testGroup")
                .currency(Currency.UAH)
                .members(Lists.newArrayList(1L, 2L)).build();

        payment = Payment.builder()
                .creatorId(1L)
                .group(group)
                .price(200D)
                .coPayers(Lists.newArrayList(1L, 2L))
                .paymentDescription("payment description")
                .build();
        group.getPayments().add(payment);
    }

    @Test
    @Sql(scripts = "classpath:scripts/add_test_group_and_payment.sql")
    void findById_GroupAndPaymentExists_ShouldReturnRightModel() {
        Payment payment = repository.findById(1L).get();

        assertEquals(1L, payment.getId());
        assertEquals("testing payment description", payment.getPaymentDescription());
        assertEquals(250, payment.getPrice());
        assertEquals(3, payment.getCoPayers().size());
        assertEquals(1L, payment.getCreatorId());
        assertEquals(1L, payment.getGroup().getId());
        assertEquals("testGroup", payment.getGroup().getGroupName());
        assertEquals(Currency.UAH, payment.getGroup().getCurrency());
        assertEquals(2, payment.getGroup().getMembers().size());
    }

    @Test
    void findById_PaymentDoesntExist_ShouldReturnNothing() {
        Optional<Payment> payment = repository.findById(1L);

        assertFalse(payment.isPresent());
    }

    @Test
    @Sql(scripts = "classpath:scripts/add_test_group.sql")
    void save_SavePaymentToGroup_ShouldBeSavedWithIdAndTimestamp() {
        Payment savedPayment = repository.save(payment);

        assertNotNull(savedPayment.getId());
        Payment found = entityManager.find(Payment.class, savedPayment.getId());

        assertNotNull(found.getTimestamp());
        assertEquals(payment.getPaymentDescription(), found.getPaymentDescription());
        assertEquals(payment.getPrice(), found.getPrice());
        assertEquals(payment.getCoPayers(), found.getCoPayers());
        assertEquals(payment.getGroup(), found.getGroup());
    }

    @Test
    @Sql(scripts = "classpath:scripts/add_test_group_and_two_payments.sql")
    void findAll_UsersExist_ReturnUsers() {
        List<Payment> found = repository.findAll();

        assertEquals(2, found.size());
    }

    @Test
    void findAll_UsersDontExist_ReturnEmptyList() {
        List<Payment> found = repository.findAll();

        assertTrue(found.isEmpty());
    }

    @Test
    @Sql(scripts = "classpath:scripts/add_test_group_and_payment.sql")
    void deleteById_DeleteExistingPayment_ShouldBeDeleted() {
        repository.deleteById(1L);

        Payment found = entityManager.find(Payment.class, 1L);
        assertNull(found);
    }

    @Test
    void deleteById_DeleteNonExistingPayment_ShouldThrowEmptyResultDataAccessException() {
        assertThrows(EmptyResultDataAccessException.class, () -> repository.deleteById(1L));
    }
}