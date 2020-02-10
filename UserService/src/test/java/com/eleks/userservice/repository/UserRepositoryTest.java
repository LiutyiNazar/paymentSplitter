package com.eleks.userservice.repository;

import com.eleks.userservice.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    UserRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("PaulMcX")
                .password("CrypthoPass")
                .firstName("Paul")
                .lastName("Liutyi")
                .dateOfBirth(LocalDate.now())
                .email("p.liutyi@eleks.com")
                .receiveNotifications(false)
                .build();
    }

    @Test
    @Sql(scripts = "classpath:scripts/add_test_user.sql")
    public void findById_UserWithIdExists_ReturnUser() {
        User found = repository.findById(1L).get();
        assertEquals(1, found.getId());
    }

    @Test
    public void findById_UserWithIdDoesntExist_ReturnNothing() {
        Optional<User> found = repository.findById(1L);
        assertFalse(found.isPresent());
    }

    @Test
    @Sql(scripts = "classpath:scripts/add_testing_users_with_ids_1_2.sql")
    public void findAll_UsersExist_ReturnUsers() {
        List<User> found = repository.findAll();
        assertEquals(2, found.size());
    }

    @Test
    public void findAll_UsersDontExist_ReturnEmptyList() {
        List<User> found = repository.findAll();
        assertTrue(found.isEmpty());
    }

    @Test
    public void save_SaveNewUserWithCorrectData_ShouldSaveAndAssignIdAndFoundById() {
        User savedUser = repository.save(user);
        assertNotNull(savedUser.getId());
        User found = entityManager.find(User.class, savedUser.getId());
        assertNotNull(found);
    }

    @Test
    @Sql(scripts = "classpath:scripts/add_test_user.sql")
    public void save_SaveUserThenUpdateWithNewCorrectData_ShouldUpdatedUser() {
        user.setId(1L);
        user.setFirstName("updated");

        User savedUser = repository.save(user);
        assertEquals(user.getId(), savedUser.getId());
        assertEquals(user.getFirstName(), savedUser.getFirstName());
    }

    @Test
    @Sql(scripts = "classpath:scripts/add_test_user.sql")
    public void deleteById_DeleteExistingUser_ShouldBeDeleted() {
        repository.deleteById(1L);
        User found = entityManager.find(User.class, 1L);
        assertNull(found);
    }

    @Test
    public void deleteById_DeleteNonExistingUser_ShouldThrowEmptyResultDataAccessException() {
        assertThrows(EmptyResultDataAccessException.class, () -> repository.deleteById(1L));
    }

    @Test
    @Sql(scripts = "classpath:scripts/add_test_user.sql")
    public void findByUsername_UserWithSuchUsernameExists_ReturnUser() {
        User found = repository.findByUsername("mcPaul").get();
        assertEquals(1, found.getId());
    }

    @Test
    public void findByUsername_UserWithSuchUsernameDoesntExist_ReturnNothing() {
        Optional<User> found = repository.findByUsername("testUser");
        assertFalse(found.isPresent());
    }

    @Test
    @Sql(scripts = "classpath:scripts/add_test_user.sql")
    public void findByEmail_UserWithSuchEmailExists_ReturnUser() {
        User found = repository.findByEmail("test.user@eleks.com").get();
        assertEquals(1, found.getId());
    }

    @Test
    public void findByEmail_UserWithSuchEmailDoesntExist_ReturnNothing() {
        Optional<User> found = repository.findByEmail("test.user@eleks.com");
        assertFalse(found.isPresent());
    }

    @Test
    @Sql(scripts = "classpath:scripts/add_testing_users_with_ids_1_2.sql")
    public void findAllByIdIn_SaveTwoUsersAndFindThemByIds_ReturnTwoUsers() {
        List<User> found = repository.findAllByIdIn(Arrays.asList(1L, 2L));
        assertEquals(2, found.size());
    }

    @Test
    public void findAllByIdIn_NoUsersAndFindByIds_ReturnEmptyList() {
        List<User> found = repository.findAllByIdIn(Arrays.asList(1L, 2L));
        assertTrue(found.isEmpty());
    }

    @Test
    @Sql(scripts = "classpath:scripts/add_testing_users_with_ids_1_2.sql")
    public void findAllByIdIn_SaveTwoUsersAndFindOneByIds_ReturnOneUsers() {
        List<User> found = repository.findAllByIdIn(Arrays.asList(2L, 3L));
        assertEquals(1, found.size());
    }
}