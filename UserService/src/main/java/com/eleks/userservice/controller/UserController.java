package com.eleks.userservice.controller;

import com.eleks.userservice.dto.UserSearchDto;
import com.eleks.userservice.dto.user.UserRequestDto;
import com.eleks.userservice.dto.user.UserResponseDto;
import com.eleks.userservice.exception.ResourceNotFoundException;
import com.eleks.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
public class UserController {
    private static final String USER_DOES_NOT_EXIST = "user with this id does't exist";

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users/{id}")
    public UserResponseDto getUser(@PathVariable Long id) {
        return userService.getUser(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_DOES_NOT_EXIST));
    }

    @GetMapping("/users")
    public List<UserResponseDto> getUsers() {
        return userService.getUsers();
    }

    @PostMapping("/users")
    @ResponseStatus(CREATED)
    public UserResponseDto saveUser(@Valid @RequestBody UserRequestDto user) {
        return userService.saveUser(user);
    }

    @PutMapping("/users/{id}")
    public UserResponseDto editUser(@PathVariable Long id, @Valid @RequestBody UserRequestDto user) {
        return userService.editUser(id, user);
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
    }

    @PostMapping("/users/search")
    public List<UserResponseDto> searchUser(@RequestBody UserSearchDto searchDto) {
        return userService.searchUsers(searchDto);
    }

}
