package com.eleks.userservice.controller;

import com.eleks.common.dto.ErrorDto;
import com.eleks.userservice.dto.UserSearchDto;
import com.eleks.userservice.dto.user.UserRequestDto;
import com.eleks.userservice.dto.user.UserResponseDto;
import com.eleks.userservice.exception.ResourceNotFoundException;
import com.eleks.userservice.exception.UniqueUserPropertiesViolationException;
import com.eleks.userservice.handler.CustomExceptionHandler;
import com.eleks.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.internal.bytebuddy.utility.RandomString;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.eleks.userservice.TestUtil.getObjectMapper;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

public class UserControllerTest {

    private MockMvc mockMvc;

    private UserService service;

    private ObjectMapper objectMapper;

    private UserRequestDto userRequestDto;

    private UserResponseDto userResponseDto;

    private static String randomString = new RandomString(51).nextString();

    @BeforeEach
    public void setUp() {
        objectMapper = getObjectMapper();

        service = mock(UserService.class);

        UserController controller = new UserController(service);

        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(new CustomExceptionHandler())
                .build();

        userRequestDto = UserRequestDto.builder()
                .username("username")
                .password("Passw0rd")
                .firstName("firstName")
                .lastName("lastName")
                .dateOfBirth(LocalDate.now())
                .email("username@eleks.com")
                .receiveNotifications(true)
                .build();

        userResponseDto = UserResponseDto.builder()
                .id(1L)
                .username(userRequestDto.getUsername())
                .firstName(userRequestDto.getFirstName())
                .lastName(userRequestDto.getLastName())
                .dateOfBirth(userRequestDto.getDateOfBirth())
                .email(userRequestDto.getEmail())
                .receiveNotifications(userRequestDto.getReceiveNotifications())
                .build();
    }

    @Test
    public void getUser_userExits_ReturnOK() throws Exception {
        when(service.getUser(userResponseDto.getId())).thenReturn(Optional.of(userResponseDto));

        mockMvc.perform(get("/users/" + userResponseDto.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(userResponseDto)));
    }


    @Test
    public void getUser_userDoesntExits_ReturnNotFoundAndError() throws Exception {
        Long id = 1L;
        when(service.getUser(id)).thenReturn(Optional.empty());

        String responseBody = mockMvc.perform(get("/users/" + id))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse().getContentAsString();

        ErrorDto error = objectMapper.readValue(responseBody, ErrorDto.class);
        assertEquals(error.getStatusCode(), HttpStatus.NOT_FOUND.value());
        assertEquals("user with this id does't exist", error.getMessages().get(0));
        assertNotNull(error.getTimestamp());
    }

    @Test
    public void getUsers_UsersExists_ReturnListOfUsers() throws Exception {
        List<UserResponseDto> list = Arrays.asList(UserResponseDto.builder().id(1L).build(),
                UserResponseDto.builder().id(2L).build(),
                UserResponseDto.builder().id(3L).build());

        when(service.getUsers()).thenReturn(list);

        mockMvc.perform(get("/users/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(list)));
    }

    @Test
    public void getUsers_NoUsers_ReturnEmptyList() throws Exception {
        List<UserResponseDto> list = Collections.emptyList();

        when(service.getUsers()).thenReturn(list);

        mockMvc.perform(get("/users/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(list)));
    }

    @Test
    public void createUser_UserDoesntExist_ReturnOKAndSavedUser() throws Exception {
        when(service.saveUser(any(UserRequestDto.class))).thenReturn(userResponseDto);

        mockMvc.perform(post("/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(userResponseDto)));
    }

    @Test
    public void createUser_DuplicatedEmailOrUsernameData_ReturnBadRequestAndError() throws Exception {
        Throwable exceptions = new UniqueUserPropertiesViolationException("msg");

        when(service.saveUser(any(UserRequestDto.class))).thenThrow(exceptions);

        String responseBody = mockMvc.perform(post("/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        ErrorDto error = objectMapper.readValue(responseBody, ErrorDto.class);

        assertEquals(error.getStatusCode(), HttpStatus.BAD_REQUEST.value());
        assertNotNull(error.getMessages());
        assertEquals(exceptions.getMessage(), error.getMessages().get(0));
        assertNotNull(error.getTimestamp());
    }

    @Test
    public void createUser_WithoutUsername_ReturnBadRequestAndError() throws Exception {
        userRequestDto.setUsername(null);
        String errorMsg = "username is required";
        postUserDataAndExpectBadRequestErrorWithSingleMsg(objectMapper.writeValueAsString(userRequestDto), errorMsg);
    }

    @Test
    public void createUser_WithBlankUsername_ReturnBadRequestAndError() throws Exception {
        userRequestDto.setUsername("");
        String errorMsg = "username length should be between 1 and 50";
        postUserDataAndExpectBadRequestErrorWithSingleMsg(objectMapper.writeValueAsString(userRequestDto), errorMsg);
    }

    @Test
    public void createUser_WithTooLongUsername_ReturnBadRequestAndError() throws Exception {
        userRequestDto.setUsername(randomString);
        String errorMsg = "username length should be between 1 and 50";
        postUserDataAndExpectBadRequestErrorWithSingleMsg(objectMapper.writeValueAsString(userRequestDto), errorMsg);
    }

    @Test
    public void createUser_WithoutFirstName_ReturnBadRequestAndError() throws Exception {
        userRequestDto.setFirstName(null);
        String errorMsg = "firstName is required";
        postUserDataAndExpectBadRequestErrorWithSingleMsg(objectMapper.writeValueAsString(userRequestDto), errorMsg);
    }

    @Test
    public void createUser_WithBlankFirstName_ReturnBadRequestAndError() throws Exception {
        userRequestDto.setFirstName("");
        String errorMsg = "firstName length should be between 1 and 50";
        postUserDataAndExpectBadRequestErrorWithSingleMsg(objectMapper.writeValueAsString(userRequestDto), errorMsg);
    }

    @Test
    public void createUser_WithTooLongFirstName_ReturnBadRequestAndError() throws Exception {
        userRequestDto.setFirstName(randomString);
        String errorMsg = "firstName length should be between 1 and 50";
        postUserDataAndExpectBadRequestErrorWithSingleMsg(objectMapper.writeValueAsString(userRequestDto), errorMsg);
    }

    @Test
    public void createUser_WithoutLastName_ReturnBadRequestAndError() throws Exception {
        userRequestDto.setLastName(null);
        String errorMsg = "lastName is required";
        postUserDataAndExpectBadRequestErrorWithSingleMsg(objectMapper.writeValueAsString(userRequestDto), errorMsg);
    }

    @Test
    public void createUser_WithBlankLastName_ReturnBadRequestAndError() throws Exception {
        userRequestDto.setLastName("");
        String errorMsg = "lastName length should be between 1 and 50";
        postUserDataAndExpectBadRequestErrorWithSingleMsg(objectMapper.writeValueAsString(userRequestDto), errorMsg);
    }

    @Test
    public void createUser_WithTooLongLastName_ReturnBadRequestAndError() throws Exception {
        userRequestDto.setLastName(randomString);
        String errorMsg = "lastName length should be between 1 and 50";
        postUserDataAndExpectBadRequestErrorWithSingleMsg(objectMapper.writeValueAsString(userRequestDto), errorMsg);
    }

    @Test
    public void createUser_WithoutBirthDate_ReturnBadRequestAndError() throws Exception {
        userRequestDto.setDateOfBirth(null);
        String errorMsg = "dateOfBirth is required";
        postUserDataAndExpectBadRequestErrorWithSingleMsg(objectMapper.writeValueAsString(userRequestDto), errorMsg);
    }

    @Test
    public void createUser_WithWrongDateFormat_ReturnBadRequestAndError() throws Exception {
        JSONObject json = new JSONObject(objectMapper.writeValueAsString(userRequestDto));
        json.put("dateOfBirth", "2019-12-19");
        String errorMsg = "Incorrect format of dateOfBirth. Valid pattern is dd-MM-yyyy";
        postUserDataAndExpectBadRequestErrorWithSingleMsg(json.toString(), errorMsg);
    }

    @Test
    public void createUser_WithoutEmail_ReturnBadRequestAndError() throws Exception {
        userRequestDto.setEmail(null);
        String errorMsg = "email is required";
        postUserDataAndExpectBadRequestErrorWithSingleMsg(objectMapper.writeValueAsString(userRequestDto), errorMsg);
    }

    @Test
    public void createUser_WithWrongEmailFormat_ReturnBadRequestAndError() throws Exception {
        JSONObject json = new JSONObject(objectMapper.writeValueAsString(userRequestDto));
        json.put("email", "#fr@gr@.com");
        String errorMsg = "email string has to be a well-formed email address";
        postUserDataAndExpectBadRequestErrorWithSingleMsg(json.toString(), errorMsg);
    }

    @Test
    public void createUser_WithoutNotificationProp_ReturnBadRequestAndError() throws Exception {
        userRequestDto.setReceiveNotifications(null);
        String errorMsg = "receiveNotifications is required";
        postUserDataAndExpectBadRequestErrorWithSingleMsg(objectMapper.writeValueAsString(userRequestDto), errorMsg);
    }

    @Test
    public void createUser_WithoutPassword_ReturnBadRequestAndError() throws Exception {
        userRequestDto.setPassword(null);
        String errorMsg = "password is required";
        postUserDataAndExpectBadRequestErrorWithSingleMsg(objectMapper.writeValueAsString(userRequestDto), errorMsg);
    }

    @Test
    public void createUser_TooSmallPassword_ReturnBadRequestAndError() throws Exception {
        userRequestDto.setPassword("1234567");
        String errorMsg = "password length should be between 8 and 50";
        postUserDataAndExpectBadRequestErrorWithSingleMsg(objectMapper.writeValueAsString(userRequestDto), errorMsg);
    }

    @Test
    public void createUser_TooLongPassword_ReturnBadRequestAndError() throws Exception {
        userRequestDto.setPassword(randomString);
        String errorMsg = "password length should be between 8 and 50";
        postUserDataAndExpectBadRequestErrorWithSingleMsg(objectMapper.writeValueAsString(userRequestDto), errorMsg);
    }

    private void postUserDataAndExpectBadRequestErrorWithSingleMsg(String content, String errorMsg) throws Exception {
        String responseBody = mockMvc.perform(post("/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        ErrorDto error = objectMapper.readValue(responseBody, ErrorDto.class);

        assertEquals(error.getStatusCode(), HttpStatus.BAD_REQUEST.value());
        assertNotNull(error.getMessages());
        assertEquals(1, error.getMessages().size());
        assertEquals(errorMsg, error.getMessages().get(0));
        assertNotNull(error.getTimestamp());
    }

    @Test
    public void createUser_InvalidNotificationProp_ReturnBadRequestAndError() throws Exception {
        JSONObject json = new JSONObject(objectMapper.writeValueAsString(userRequestDto));
        json.put("receiveNotifications", "#fr@gr@.com");
        String errorMsg = "username can't be null";

        String responseBody = mockMvc.perform(post("/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        ErrorDto error = objectMapper.readValue(responseBody, ErrorDto.class);

        assertEquals(error.getStatusCode(), HttpStatus.BAD_REQUEST.value());
        assertNotNull(error.getMessages());
        assertEquals(1, error.getMessages().size());
        assertNotNull(error.getTimestamp());
    }

    @Test
    public void editUser_UserExists_ReturnOkAndUpdatedData() throws Exception {
        when(service.editUser(anyLong(), any(UserRequestDto.class))).thenReturn(userResponseDto);

        mockMvc.perform(put("/users/" + userResponseDto.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(userResponseDto)));
    }

    @Test
    public void editUser_UserDoesntExist_ReturnNotFoundAndError() throws Exception {

        when(service.editUser(anyLong(), any(UserRequestDto.class))).thenThrow(ResourceNotFoundException.class);

        String responseBody = mockMvc.perform(put("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        ErrorDto error = objectMapper.readValue(responseBody, ErrorDto.class);

        assertEquals(error.getStatusCode(), HttpStatus.NOT_FOUND.value());
        assertNotNull(error.getMessages());
        assertEquals(1, error.getMessages().size());
        assertNotNull(error.getTimestamp());
    }

    @Test
    public void editUser_WithoutUsername_ReturnBadRequestAndError() throws Exception {
        userRequestDto.setUsername(null);
        String errorMsg = "username is required";
        putUserDataAndExpectBadRequestErrorWithSingleMsg(objectMapper.writeValueAsString(userRequestDto), errorMsg);
    }

    @Test
    public void editUser_WithBlankUsername_ReturnBadRequestAndError() throws Exception {
        userRequestDto.setUsername("");
        String errorMsg = "username length should be between 1 and 50";
        putUserDataAndExpectBadRequestErrorWithSingleMsg(objectMapper.writeValueAsString(userRequestDto), errorMsg);
    }

    @Test
    public void editUser_WithTooLongUsername_ReturnBadRequestAndError() throws Exception {
        userRequestDto.setUsername(randomString);
        String errorMsg = "username length should be between 1 and 50";
        putUserDataAndExpectBadRequestErrorWithSingleMsg(objectMapper.writeValueAsString(userRequestDto), errorMsg);
    }

    @Test
    public void editUser_WithoutFirstName_ReturnBadRequestAndError() throws Exception {
        userRequestDto.setFirstName(null);
        String errorMsg = "firstName is required";
        putUserDataAndExpectBadRequestErrorWithSingleMsg(objectMapper.writeValueAsString(userRequestDto), errorMsg);
    }

    @Test
    public void editUser_WithBlankFirstName_ReturnBadRequestAndError() throws Exception {
        userRequestDto.setFirstName("");
        String errorMsg = "firstName length should be between 1 and 50";
        putUserDataAndExpectBadRequestErrorWithSingleMsg(objectMapper.writeValueAsString(userRequestDto), errorMsg);
    }

    @Test
    public void editUser_WithTooLongFirstName_ReturnBadRequestAndError() throws Exception {
        userRequestDto.setFirstName(randomString);
        String errorMsg = "firstName length should be between 1 and 50";
        putUserDataAndExpectBadRequestErrorWithSingleMsg(objectMapper.writeValueAsString(userRequestDto), errorMsg);
    }

    @Test
    public void editUser_WithoutLastName_ReturnBadRequestAndError() throws Exception {
        userRequestDto.setLastName(null);
        String errorMsg = "lastName is required";
        putUserDataAndExpectBadRequestErrorWithSingleMsg(objectMapper.writeValueAsString(userRequestDto), errorMsg);
    }

    @Test
    public void editUser_WithBlankLastName_ReturnBadRequestAndError() throws Exception {
        userRequestDto.setLastName("");
        String errorMsg = "lastName length should be between 1 and 50";
        putUserDataAndExpectBadRequestErrorWithSingleMsg(objectMapper.writeValueAsString(userRequestDto), errorMsg);
    }

    @Test
    public void editUser_WithTooLongLastName_ReturnBadRequestAndError() throws Exception {
        userRequestDto.setLastName(randomString);
        String errorMsg = "lastName length should be between 1 and 50";
        putUserDataAndExpectBadRequestErrorWithSingleMsg(objectMapper.writeValueAsString(userRequestDto), errorMsg);
    }

    @Test
    public void editUser_WithoutBirthDate_ReturnBadRequestAndError() throws Exception {
        userRequestDto.setDateOfBirth(null);
        String errorMsg = "dateOfBirth is required";
        putUserDataAndExpectBadRequestErrorWithSingleMsg(objectMapper.writeValueAsString(userRequestDto), errorMsg);
    }

    @Test
    public void editUser_WithWrongDateFormat_ReturnBadRequestAndError() throws Exception {
        JSONObject json = new JSONObject(objectMapper.writeValueAsString(userRequestDto));
        json.put("dateOfBirth", "2019-12-19");
        String errorMsg = "Incorrect format of dateOfBirth. Valid pattern is dd-MM-yyyy";
        putUserDataAndExpectBadRequestErrorWithSingleMsg(json.toString(), errorMsg);
    }

    @Test
    public void editUser_WithoutEmail_ReturnBadRequestAndError() throws Exception {
        userRequestDto.setEmail(null);
        String errorMsg = "email is required";
        putUserDataAndExpectBadRequestErrorWithSingleMsg(objectMapper.writeValueAsString(userRequestDto), errorMsg);
    }

    @Test
    public void editUser_WithWrongEmailFormat_ReturnBadRequestAndError() throws Exception {
        JSONObject json = new JSONObject(objectMapper.writeValueAsString(userRequestDto));
        json.put("email", "#fr@gr@.com");
        String errorMsg = "email string has to be a well-formed email address";
        putUserDataAndExpectBadRequestErrorWithSingleMsg(json.toString(), errorMsg);
    }

    @Test
    public void editUser_WithoutNotificationProp_ReturnBadRequestAndError() throws Exception {
        userRequestDto.setReceiveNotifications(null);
        String errorMsg = "receiveNotifications is required";
        putUserDataAndExpectBadRequestErrorWithSingleMsg(objectMapper.writeValueAsString(userRequestDto), errorMsg);
    }

    @Test
    public void editUser_WithoutPassword_ReturnBadRequestAndError() throws Exception {
        userRequestDto.setPassword(null);
        String errorMsg = "password is required";
        putUserDataAndExpectBadRequestErrorWithSingleMsg(objectMapper.writeValueAsString(userRequestDto), errorMsg);
    }

    @Test
    public void editUser_WithTooSmallPassword_ReturnBadRequestAndError() throws Exception {
        userRequestDto.setPassword("1234567");
        String errorMsg = "password length should be between 8 and 50";
        putUserDataAndExpectBadRequestErrorWithSingleMsg(objectMapper.writeValueAsString(userRequestDto), errorMsg);
    }

    @Test
    public void editUser_WithTooLongPassword_ReturnBadRequestAndError() throws Exception {
        userRequestDto.setPassword(randomString);
        String errorMsg = "password length should be between 8 and 50";
        putUserDataAndExpectBadRequestErrorWithSingleMsg(objectMapper.writeValueAsString(userRequestDto), errorMsg);
    }

    private void putUserDataAndExpectBadRequestErrorWithSingleMsg(String content, String errorMsg) throws Exception {
        String responseBody = mockMvc.perform(put("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        ErrorDto error = objectMapper.readValue(responseBody, ErrorDto.class);

        assertEquals(error.getStatusCode(), HttpStatus.BAD_REQUEST.value());
        assertNotNull(error.getMessages());
        assertEquals(1, error.getMessages().size());
        assertEquals(errorMsg, error.getMessages().get(0));
        assertNotNull(error.getTimestamp());
    }

    @Test
    public void deleteUser_UserExists_ReturnOk() throws Exception {
        Long id = 1L;
        mockMvc.perform(delete("/users/" + id))
                .andExpect(status().isOk());

        verify(service).deleteUserById(eq(id));
    }

    @Test
    public void deleteUser_UserDoesntExist_ReturnNotFoundAndError() throws Exception {
        Long id = 1L;
        ResourceNotFoundException ex = new ResourceNotFoundException("msg");
        doThrow(ex).when(service).deleteUserById(id);

        String responseBody = mockMvc.perform(delete("/users/" + id))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        ErrorDto error = objectMapper.readValue(responseBody, ErrorDto.class);

        assertEquals(error.getStatusCode(), HttpStatus.NOT_FOUND.value());
        assertNotNull(error.getMessages());
        assertEquals(ex.getMessage(), error.getMessages().get(0));
        assertNotNull(error.getTimestamp());
    }

    @Test
    public void searchUsers_PostSearchParams_ShouldReturnOkAndListOfResults() throws Exception {
        UserSearchDto searchDto = new UserSearchDto(Collections.singletonList(1L));
        List<UserResponseDto> result = Collections.singletonList(UserResponseDto.builder().id(1L).build());

        when(service.searchUsers(any(UserSearchDto.class))).thenReturn(result);

        mockMvc.perform(post("/users/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchDto)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(result)));
    }

    @Test
    public void searchUsers_PostSearchParams_ShouldReturnOkAndEmptyList() throws Exception {
        UserSearchDto searchDto = new UserSearchDto(Collections.singletonList(1L));

        when(service.searchUsers(any(UserSearchDto.class))).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/users/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchDto)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(Collections.emptyList())));
    }

}