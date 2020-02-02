package com.eleks.userservice.service;

import com.eleks.userservice.domain.User;
import com.eleks.userservice.dto.UserSearchDto;
import com.eleks.userservice.dto.user.UserRequestDto;
import com.eleks.userservice.dto.user.UserResponseDto;
import com.eleks.userservice.exception.ResourceNotFoundException;
import com.eleks.userservice.exception.UniqueUserPropertiesViolationException;
import com.eleks.userservice.mapper.UserMapper;
import com.eleks.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private UserRepository repository;
    private PasswordEncoder encoder;

    @Autowired
    public UserServiceImpl(UserRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    @Override
    public Optional<UserResponseDto> getUser(Long id) {
        return repository.findById(id).map(UserMapper::toDto);
    }

    @Override
    public List<UserResponseDto> getUsers() {
        return repository.findAll()
                .stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDto saveUser(UserRequestDto user) throws UniqueUserPropertiesViolationException {
        if (repository.findByUsername(user.getUsername()).isPresent()) {
            throw new UniqueUserPropertiesViolationException("user with this username already exists");
        } else if (repository.findByEmail(user.getEmail()).isPresent()) {
            throw new UniqueUserPropertiesViolationException("user with this email already exists");
        } else {
            User entity = UserMapper.toEntity(user);

            entity.setPassword(encoder.encode(entity.getPassword()));

            User savedEntity = repository.save(entity);
            return UserMapper.toDto(savedEntity);
        }
    }

    @Override
    public UserResponseDto editUser(Long id, UserRequestDto user) {
        if (repository.findById(id).isPresent()) {
            User entity = UserMapper.toEntity(user);
            entity.setId(id);
            User saved = repository.save(entity);
            return UserMapper.toDto(saved);
        } else {
            throw new ResourceNotFoundException("user with this id does't exist");
        }
    }

    @Override
    public List<UserResponseDto> searchUsers(UserSearchDto searchDto) {
        List<User> users = repository.findAllByIdIn(searchDto.getUserIds());
        return users.stream().map(UserMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public void deleteUserById(Long id) {
        if (repository.findById(id).isPresent()) {
            repository.deleteById(id);
        } else {
            throw new ResourceNotFoundException("user with this id does't exist");
        }
    }
}
