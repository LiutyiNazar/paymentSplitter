package com.eleks.userservice.controller;

import com.eleks.common.dto.ErrorDto;
import com.eleks.common.security.JwtTokenUtil;
import com.eleks.common.security.model.JwtUserDataClaim;
import com.eleks.userservice.dto.UserDetailsImpl;
import com.eleks.userservice.dto.login.JwtResponse;
import com.eleks.userservice.dto.login.LoginRequest;
import com.eleks.userservice.handler.CustomExceptionHandler;
import com.eleks.userservice.service.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import static com.eleks.userservice.TestUtil.getObjectMapper;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

public class AuthControllerTest {

    MockMvc mockMvc;

    JwtTokenUtil jwtTokenUtil;
    UserDetailsServiceImpl service;
    AuthenticationManager authenticationManager;

    AuthController controller;
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        jwtTokenUtil = mock(JwtTokenUtil.class);
        service = mock(UserDetailsServiceImpl.class);
        authenticationManager = mock(AuthenticationManager.class);

        controller = new AuthController(jwtTokenUtil, service, authenticationManager);
        objectMapper = getObjectMapper();

        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(new CustomExceptionHandler())
                .build();
    }

    @Test
    void login_ValidCredentials_ShouldReturnOkAndToken() throws Exception {
        LoginRequest request = new LoginRequest("testUser", "Passw0rd");
        JwtResponse response = new JwtResponse("token");
        UserDetailsImpl userDetails = new UserDetailsImpl("testUser", "Passw0rd_encoded", 1L);

        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(service.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(jwtTokenUtil.generateToken(any(JwtUserDataClaim.class))).thenReturn(response.getJwt());

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    void login_InvalidCredentials_ShouldReturnUnauthorizedAndError() throws Exception {
        LoginRequest request = new LoginRequest("testUser", "Passw0rd");

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("msg"));

        doPostLoginAndValidateError(objectMapper.writeValueAsString(request), HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }

    @Test
    void login_UsernameNotPassed_ShouldReturnBadRequestAndError() throws Exception {
        LoginRequest request = new LoginRequest(null, "Passw0rd");
        doPostLoginAndValidateError(objectMapper.writeValueAsString(request), HttpStatus.BAD_REQUEST, "username is required");
    }

    @Test
    void login_PasswordNotPassed_ShouldReturnBadRequestAndError() throws Exception {
        LoginRequest request = new LoginRequest("testUser", null);
        doPostLoginAndValidateError(objectMapper.writeValueAsString(request), HttpStatus.BAD_REQUEST, "password is required");
    }

    private void doPostLoginAndValidateError(String content, HttpStatus status, String errorMsg) throws Exception {
        String response = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
                .andExpect(status().is(status.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        ErrorDto error = objectMapper.readValue(response, ErrorDto.class);

        assertEquals(error.getStatusCode(), status.value());
        assertEquals(errorMsg, error.getMessages().get(0));
        assertNotNull(error.getTimestamp());
    }
}
