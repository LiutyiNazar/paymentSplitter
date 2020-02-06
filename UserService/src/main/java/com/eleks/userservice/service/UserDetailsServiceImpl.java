package com.eleks.userservice.service;

import com.eleks.userservice.domain.User;
import com.eleks.userservice.dto.UserDetailsImpl;
import com.eleks.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDate;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private UserRepository repository;

    @Autowired
    public UserDetailsServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetailsImpl loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new UserDetailsImpl(user.getUsername(), user.getPassword(), user.getId());
    }
}
