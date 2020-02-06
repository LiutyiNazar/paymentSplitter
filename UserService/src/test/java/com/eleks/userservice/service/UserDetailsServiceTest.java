package com.eleks.userservice.service;

import com.eleks.userservice.domain.User;
import com.eleks.userservice.dto.UserDetailsImpl;
import com.eleks.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceTest {

    @Mock
    UserRepository repository;

    @InjectMocks
    UserDetailsServiceImpl service;

    @Test
    public void loadUserByUsername_UserWithSuchUsernameExists_ShouldReturnDetailModel() {
        User user = User.builder()
                .id(1L)
                .username("mcPaul")
                .password("CryptPass")
                .firstName("Paul")
                .lastName("mcXerox")
                .dateOfBirth(LocalDate.now())
                .email("p.xerox@gmail.com")
                .receiveNotifications(true)
                .build();

        when(repository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        UserDetailsImpl userDetails = service.loadUserByUsername(user.getUsername());

        assertEquals(user.getUsername(), userDetails.getUsername());
        assertEquals(user.getPassword(), userDetails.getPassword());
        assertEquals(user.getId(), userDetails.getUserId());
    }

    @Test
    public void loadUserByUsername_UserWithSuchUsernameDoesntExist_ShouldThrowUsernameNotFoundException() {
        when(repository.findByUsername("mcPaul")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("mcPaul"));
    }
}