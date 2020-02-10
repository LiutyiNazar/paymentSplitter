package com.eleks.common.config;


import com.eleks.common.auth.AuthRequestFilter;
import com.eleks.common.auth.AuthenticationEntryPointImpl;
import com.eleks.common.security.JwtTokenService;
import com.eleks.common.security.SecurityPrincipalHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private ObjectMapper objectMapper;
    private JwtTokenService jwtTokenService;
    private SecurityPrincipalHolder principalHolder;

    public SecurityConfig(ObjectMapper objectMapper, JwtTokenService jwtTokenService, SecurityPrincipalHolder principalHolder) {
        this.objectMapper = objectMapper;
        this.jwtTokenService = jwtTokenService;
        this.principalHolder = principalHolder;
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf().disable()
                .authorizeRequests().anyRequest().authenticated()
                .and()
                .exceptionHandling().authenticationEntryPoint(new AuthenticationEntryPointImpl(objectMapper))
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterBefore(new AuthRequestFilter(jwtTokenService, principalHolder), UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers(
                "/v2/api-docs",
                "/swagger-resources",
                "/swagger-resources/**",
                "/configuration/ui",
                "/configuration/security",
                "/users",
                "/actuator/**",
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/webjars/**"
        );

        for (Map.Entry<HttpMethod, List<String>> endpoint : getEndpointsToIgnore()) {
            web.ignoring().antMatchers(endpoint.getKey(), endpoint.getValue().toArray(new String[0]));
        }
    }

    protected List<Map.Entry<HttpMethod, List<String>>> getEndpointsToIgnore() {
        return Collections.emptyList();
    }
}