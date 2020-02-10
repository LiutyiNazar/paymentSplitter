package com.eleks.userservice.controller;


import com.eleks.common.security.JwtTokenService;
import com.eleks.common.security.model.JwtUserDataClaim;
import com.eleks.userservice.dto.UserDetailsImpl;
import com.eleks.userservice.dto.login.JwtResponse;
import com.eleks.userservice.dto.login.LoginRequest;
import com.eleks.userservice.service.UserDetailsServiceImpl;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@Slf4j
@Api(value = "Auth", description = "Auth API")
public class AuthController {

    private JwtTokenService jwtTokenService;
    private UserDetailsServiceImpl service;
    private AuthenticationManager authenticationManager;

    @Autowired
    public AuthController(JwtTokenService jwtTokenService, UserDetailsServiceImpl service, AuthenticationManager authenticationManager) {
        this.jwtTokenService = jwtTokenService;
        this.service = service;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping(value = "/login")
    public JwtResponse login(@Valid @RequestBody LoginRequest request) throws IOException, BadCredentialsException {
        performAuthentication(request.getUsername(), request.getPassword());

        UserDetailsImpl details = service.loadUserByUsername(request.getUsername());
        JwtUserDataClaim userDataClaim = new JwtUserDataClaim(details.getUsername(), details.getUserId());

        return new JwtResponse(jwtTokenService.generateToken(userDataClaim));
    }

    private void performAuthentication(String username, String password) throws BadCredentialsException {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid credentials");
        }
    }
}
