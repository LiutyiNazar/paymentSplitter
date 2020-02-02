package com.eleks.userservice.controller;

import com.eleks.userservice.dto.UserSearchDto;
import com.eleks.userservice.dto.user.UserRequestDto;
import com.eleks.userservice.dto.user.UserResponseDto;
import com.eleks.userservice.exception.ResourceNotFoundException;
import com.eleks.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
public class UserController {

    private UserService service;

    @Autowired
    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/users/{id}")
    public UserResponseDto getUser(@PathVariable Long id) {
        return service.getUser(id).orElseThrow(() -> new ResourceNotFoundException("user with this id does't exist"));
    }

    @GetMapping("/users")
    public List<UserResponseDto> getUsers() {
        return service.getUsers();
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDto saveUser(@Valid @RequestBody UserRequestDto user) {
        return service.saveUser(user);
    }

    @PutMapping("/users/{id}")
    public UserResponseDto editUser(@PathVariable Long id, @Valid @RequestBody UserRequestDto user) {

        return service.editUser(id, user);
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable Long id) {
        service.deleteUserById(id);
    }

    @PostMapping("/users/search")
    public List<UserResponseDto> searchUser(@RequestBody UserSearchDto searchDto) {
        return service.searchUsers(searchDto);
    }

}
