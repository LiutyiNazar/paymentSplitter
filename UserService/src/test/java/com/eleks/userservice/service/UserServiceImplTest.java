package com.eleks.userservice.service;

import com.eleks.userservice.domain.User;
import com.eleks.userservice.dto.UserSearchDto;
import com.eleks.userservice.dto.user.UserRequestDto;
import com.eleks.userservice.dto.user.UserResponseDto;
import com.eleks.userservice.exception.ResourceNotFoundException;
import com.eleks.userservice.exception.UniqueUserPropertiesViolationException;
import com.eleks.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private UserServiceImpl service;

    private User user;
    private UserRequestDto userRequestDto;

    @BeforeEach
    void setUp() {
        userRequestDto = UserRequestDto.builder()
                .username("username")
                .password("decoded_password")
                .firstName("firstName")
                .lastName("lastName")
                .dateOfBirth(LocalDate.now())
                .email("email@eleks.com")
                .receiveNotifications(true)
                .build();

        user = User.builder()
                .id(1L)
                .username(userRequestDto.getUsername())
                .password(userRequestDto.getPassword())
                .firstName(userRequestDto.getFirstName())
                .lastName(userRequestDto.getLastName())
                .dateOfBirth(userRequestDto.getDateOfBirth())
                .email(userRequestDto.getEmail())
                .receiveNotifications(userRequestDto.getReceiveNotifications())
                .build();
    }

    @Test
    public void getUser_UserWithIdExists_ReturnUserModel() {
        when(repository.findById(user.getId())).thenReturn(Optional.of(user));

        Optional<UserResponseDto> result = service.getUser(user.getId());

        assertTrue(result.isPresent());
    }

    @Test
    public void getUser_UserWithIdDoesntExist_ReturnNothing() {
        when(repository.findById(user.getId())).thenReturn(Optional.empty());

        Optional<UserResponseDto> result = service.getUser(user.getId());

        assertFalse(result.isPresent());
    }

    @Test
    public void getUsers_ListOfUsersExists_ReturnList() {
        List<User> repoList = Arrays.asList(
                User.builder().id(1L).build(),
                User.builder().id(2L).build(),
                User.builder().id(3L).build()
        );

        when(repository.findAll()).thenReturn(repoList);

        assertEquals(repoList.size(), service.getUsers().size());
    }

    @Test
    public void getUsers_NoUsersInRepository_ReturnEmptyList() {
        List<User> repoList = Collections.emptyList();

        when(repository.findAll()).thenReturn(repoList);

        assertEquals(0, service.getUsers().size());
    }

    @Test
    public void saveUser_UserWithSuchUsernameOrEmailDoesntExist_ReturnSavedUser() {
        when(repository.save(any(User.class))).thenReturn(user);
        when(repository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(repository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(encoder.encode(anyString())).thenReturn("encoded_password");

        UserResponseDto responseDto = service.saveUser(userRequestDto);

        assertNotNull(responseDto);
    }

    @Test
    public void saveUser_UserWithSuchUsernameAlreadyExists_ThrowUniqueUserPropertiesViolationException() {
        when(repository.findByUsername(anyString())).thenReturn(Optional.of(user));

        Throwable throwable = assertThrows(UniqueUserPropertiesViolationException.class, () -> service.saveUser(userRequestDto));
        assertEquals("user with this username already exists", throwable.getMessage());
    }

    @Test
    public void saveUser_UserWithSuchEmailAlreadyExists_ThrowError() {
        when(repository.findByEmail(anyString())).thenReturn(Optional.of(user));

        Throwable throwable = assertThrows(UniqueUserPropertiesViolationException.class, () -> service.saveUser(userRequestDto));
        assertEquals("user with this email already exists", throwable.getMessage());
    }

    @Test
    public void editUser_UserIdExists_ReturnResponseDto() {
        Long id = 1L;
        when(repository.findById(id)).thenReturn(Optional.of(user));
        when(repository.save(any(User.class))).thenReturn(user);

        UserResponseDto responseDto = service.editUser(id, userRequestDto);

        assertNotNull(responseDto);
        assertEquals(id, responseDto.getId());
    }

    @Test
    public void editUser_UserIdDoesntExist_ThrowResourceNotFoundException() {
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> service.editUser(1L, userRequestDto));
        assertEquals("user with this id does't exist", exception.getMessage());
    }

    @Test
    public void deleteById_UserWithIdExists_CallRepositoryDelete() {
        Long id = 1L;
        when(repository.findById(id)).thenReturn(Optional.of(user));

        service.deleteUserById(id);

        verify(repository).deleteById(id);
    }

    @Test
    public void deleteById_UserWithIdDoesntExist_ThrowResourceNotExists() {
        Long id = 1L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> service.deleteUserById(id));

        assertEquals("user with this id does't exist", exception.getMessage());
    }

    @Test
    public void searchUsers_RepositoryReturnsNotEmptyList_ShouldReturnListOfResponses() {
        List<Long> ids = Arrays.asList(1L, 2L);
        List<User> repoList = Arrays.asList(
                User.builder().id(1L).build(),
                User.builder().id(2L).build()
        );

        when(repository.findAllByIdIn(ids)).thenReturn(repoList);

        List<UserResponseDto> responseList = service.searchUsers(new UserSearchDto(ids));

        assertEquals(repoList.size(), responseList.size());
    }

    @Test
    public void searchUsers_RepositoryReturnsNothing_ShouldReturnEmptyListOfResponses() {
        List<Long> ids = Arrays.asList(1L, 2L);

        when(repository.findAllByIdIn(ids)).thenReturn(Collections.emptyList());

        List<UserResponseDto> responseList = service.searchUsers(new UserSearchDto(ids));

        assertTrue(responseList.isEmpty());
    }
}