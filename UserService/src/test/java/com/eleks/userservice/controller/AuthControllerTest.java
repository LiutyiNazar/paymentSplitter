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
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

public class AuthControllerTest {

    private MockMvc mockMvc;

    private JwtTokenUtil jwtTokenUtil;
    private UserDetailsServiceImpl service;
    private AuthenticationManager authenticationManager;

    private AuthController controller;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = getObjectMapper();
        authenticationManager = mock(AuthenticationManager.class);
        service = mock(UserDetailsServiceImpl.class);
        jwtTokenUtil = mock(JwtTokenUtil.class);
        controller = new AuthController(jwtTokenUtil, service, authenticationManager);
        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(new CustomExceptionHandler())
                .build();
    }

    @Test
    void login_ValidCredentials_ShouldReturnOkAndToken() throws Exception {
        LoginRequest request = new LoginRequest("Peter", "Pooh");
        JwtResponse response = new JwtResponse("token");
        UserDetailsImpl userDetails = new UserDetailsImpl("Peter", "Pooh_encoded", 1L);

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
    public void login_InvalidCredentials_ShouldReturnUnauthorizedAndError() throws Exception {
        LoginRequest request = new LoginRequest("Paul", "CrypthoPass");

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("msg"));

        performLoginOfUserAndCheckReceivedError(objectMapper.writeValueAsString(request), UNAUTHORIZED, "Invalid credentials");
    }

    @Test
    public void login_UsernameNotPassed_ShouldReturnBadRequestAndError() throws Exception {
        LoginRequest request = new LoginRequest(null, "CrypthoPass");
        performLoginOfUserAndCheckReceivedError(objectMapper.writeValueAsString(request), BAD_REQUEST, "username is required");
    }

    @Test
    public void login_PasswordNotPassed_ShouldReturnBadRequestAndError() throws Exception {
        LoginRequest request = new LoginRequest("Paul", null);
        performLoginOfUserAndCheckReceivedError(objectMapper.writeValueAsString(request), BAD_REQUEST, "password is required");
    }

    private void performLoginOfUserAndCheckReceivedError(String content, HttpStatus status, String errorMsg) throws Exception {
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
