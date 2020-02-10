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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class GroupRepositoryTest {

    @Autowired
    private GroupRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private Group group;

    @BeforeEach
    void setUp() {
        group = Group.builder()
                .groupName("groupName")
                .currency(Currency.EUR)
                .members(Lists.newArrayList(33L, 44L, 55L)).build();
    }

    @Test
    void saveSaveGroupWithoutIdShouldReturnSavedWithId() {
        Group saved = repository.save(group);

        Group found = entityManager.find(Group.class, saved.getId());

        assertNotNull(saved.getId());
        assertEquals(group.getGroupName(), found.getGroupName());
        assertEquals(group.getCurrency(), found.getCurrency());
        assertEquals(group.getMembers(), found.getMembers());
    }

    @Test
    @Sql(scripts = "classpath:scripts/add_test_group.sql")
    void saveUpdateGroupWithNewDataShouldReturnUpdatedGroup() {
        group.setId(1L);

        Group updated = repository.save(group);

        assertEquals(group.getGroupName(), updated.getGroupName());
        assertEquals(group.getCurrency(), updated.getCurrency());
        assertEquals(group.getMembers(), updated.getMembers());
    }

    @Test
    void saveGroupWithoutNameShouldThrowDataIntegrityViolationException() {
        group.setGroupName(null);
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.save(group));
    }

    @Test
    void saveGroupWithoutCurrencyShouldThrowDataIntegrityViolationException() {
        group.setCurrency(null);
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.save(group));
    }

    @Test
    @Sql(scripts = "classpath:scripts/add_test_group.sql")
    void findByIdGroupWithIdExistsReturnUser() {
        Group found = repository.findById(1L).get();

        assertEquals(1, found.getId());
        assertEquals("testGroup", found.getGroupName());
        assertEquals(Currency.UAH, found.getCurrency());
        assertEquals(Lists.newArrayList(1L, 2L), found.getMembers());
    }

    @Test
    void findByIdGroupWithIdDoesntExistReturnNothing() {
        Optional<Group> found = repository.findById(1L);

        assertFalse(found.isPresent());
    }

    @Test
    @Sql(scripts = "classpath:scripts/add_test_group.sql")
    void deleteByIdDeleteExistingGroupShouldBeDeleted() {
        repository.deleteById(1L);

        Group found = entityManager.find(Group.class, 1L);
        assertNull(found);
    }

    @Test
    void deleteByIdDeleteNonExistingGroupShouldThrowEmptyResultDataAccessException() {
        assertThrows(EmptyResultDataAccessException.class, () -> repository.deleteById(1L));
    }

    @Test
    @Sql(scripts = "classpath:scripts/add_test_group_and_two_payments.sql")
    void deleteByIdDeleteExistingGroupShouldDeleteTwoPayments() {
        repository.deleteById(1L);

        Payment payment1 = entityManager.find(Payment.class, 1L);
        Payment payment2 = entityManager.find(Payment.class, 2L);

        assertNull(payment1);
        assertNull(payment2);
    }
}