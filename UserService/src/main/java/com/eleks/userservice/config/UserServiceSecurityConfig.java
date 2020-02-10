package com.eleks.userservice.config;

import com.eleks.common.config.SecurityConfig;
import com.eleks.common.security.JwtTokenService;
import com.eleks.common.security.SecurityPrincipalHolder;
import com.eleks.userservice.service.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static java.util.Arrays.asList;
import static org.springframework.http.HttpMethod.POST;

@Configuration
@EnableWebSecurity
@ComponentScan(basePackages = "com.eleks")
public class UserServiceSecurityConfig extends SecurityConfig {

    private UserDetailsServiceImpl userDetailsService;

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    public UserServiceSecurityConfig(ObjectMapper objectMapper, JwtTokenService jwtTokenService, UserDetailsServiceImpl userDetailsService, SecurityPrincipalHolder holder) {
        super(objectMapper, jwtTokenService, holder);
        this.userDetailsService = userDetailsService;
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected List<Map.Entry<HttpMethod, List<String>>> getEndpointsToIgnore() {
        Map.Entry<HttpMethod, List<String>> endpoints = new AbstractMap.SimpleEntry<>(POST, asList("/login", "/users"));
        return Collections.singletonList(endpoints);
    }
}
