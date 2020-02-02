package com.eleks.groupservice.repository;

import com.eleks.groupservice.domain.Currency;
import com.eleks.groupservice.domain.Group;
import com.eleks.groupservice.domain.Payment;
import com.google.common.collect.Sets;
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

import java.util.List;
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
    void save_SaveGroupWithoutId_ShouldReturnSavedWithId() {
        Group saved = repository.save(group);

        Group found = entityManager.find(Group.class, saved.getId());

        assertNotNull(saved.getId());
        assertEquals(group.getGroupName(), found.getGroupName());
        assertEquals(group.getCurrency(), found.getCurrency());
        assertEquals(group.getMembers(), found.getMembers());
    }

    @Test
    @Sql(scripts = "classpath:scripts/insert_test_group.sql")
    void save_UpdateGroupWithNewData_ShouldReturnUpdatedGroup() {
        group.setId(1L);

        Group updated = repository.save(group);

        assertEquals(group.getGroupName(), updated.getGroupName());
        assertEquals(group.getCurrency(), updated.getCurrency());
        assertEquals(group.getMembers(), updated.getMembers());
    }

    @Test
    void save_GroupWithoutName_ShouldThrowDataIntegrityViolationException() {
        group.setGroupName(null);
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.save(group));
    }

    @Test
    void save_GroupWithoutCurrency_ShouldThrowDataIntegrityViolationException() {
        group.setCurrency(null);
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.save(group));
    }

    @Test
    @Sql(scripts = "classpath:scripts/insert_test_group.sql")
    void findById_GroupWithIdExists_ReturnUser() {
        Group found = repository.findById(1L).get();

        assertEquals(1, found.getId());
        assertEquals("testGroup", found.getGroupName());
        assertEquals(Currency.UAH, found.getCurrency());
        assertEquals(Lists.newArrayList(1L, 2L), found.getMembers());
    }

    @Test
    void findById_GroupWithIdDoesntExist_ReturnNothing() {
        Optional<Group> found = repository.findById(1L);

        assertFalse(found.isPresent());
    }

    @Test
    @Sql(scripts = "classpath:scripts/insert_test_group.sql")
    void deleteById_DeleteExistingGroup_ShouldBeDeleted() {
        repository.deleteById(1L);

        Group found = entityManager.find(Group.class, 1L);
        assertNull(found);
    }

    @Test
    void deleteById_DeleteNonExistingGroup_ShouldThrowEmptyResultDataAccessException() {
        assertThrows(EmptyResultDataAccessException.class, () -> repository.deleteById(1L));
    }

    @Test
    @Sql(scripts = "classpath:scripts/insert_test_group_and_two_payments.sql")
    void deleteById_DeleteExistingGroup_ShouldDeleteTwoPayments() {
        repository.deleteById(1L);

        Payment payment1 = entityManager.find(Payment.class, 1L);
        Payment payment2 = entityManager.find(Payment.class, 2L);

        assertNull(payment1);
        assertNull(payment2);
    }
}