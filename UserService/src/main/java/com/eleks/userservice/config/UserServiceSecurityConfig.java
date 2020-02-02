package com.eleks.userservice.config;

import com.eleks.common.security.BaseSecurityConfig;
import com.eleks.common.security.JwtTokenUtil;
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

@Configuration
@EnableWebSecurity
@ComponentScan(basePackages = "com.eleks")
public class UserServiceSecurityConfig extends BaseSecurityConfig {

    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    public UserServiceSecurityConfig(ObjectMapper objectMapper, JwtTokenUtil jwtTokenUtil, UserDetailsServiceImpl userDetailsService, SecurityPrincipalHolder holder) {
        super(objectMapper, jwtTokenUtil, holder);
        this.userDetailsService = userDetailsService;
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected List<Map.Entry<HttpMethod, List<String>>> getEndpointsToIgnore() {
        Map.Entry<HttpMethod, List<String>> endpoints = new AbstractMap.SimpleEntry<>(HttpMethod.POST, Arrays.asList("/login", "/users"));
        return Collections.singletonList(endpoints);
    }
}
