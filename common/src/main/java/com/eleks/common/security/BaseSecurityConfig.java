package com.eleks.common.security;


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

public class BaseSecurityConfig extends WebSecurityConfigurerAdapter {

    private ObjectMapper objectMapper;
    private JwtTokenUtil jwtTokenUtil;
    private SecurityPrincipalHolder principalHolder;

    public BaseSecurityConfig(ObjectMapper objectMapper, JwtTokenUtil jwtTokenUtil, SecurityPrincipalHolder principalHolder) {
        this.objectMapper = objectMapper;
        this.jwtTokenUtil = jwtTokenUtil;
        this.principalHolder = principalHolder;
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf().disable()
                .authorizeRequests().anyRequest().authenticated()
                .and()
                .exceptionHandling().authenticationEntryPoint(new AuthEntryPointImpl(objectMapper))
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterBefore(new AuthRequestFilter(jwtTokenUtil, principalHolder), UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(SWAGGER_WHITELIST);
        for (Map.Entry<HttpMethod, List<String>> endpoint : getEndpointsToIgnore()) {
            web.ignoring().antMatchers(endpoint.getKey(), endpoint.getValue().toArray(new String[0]));
        }
    }

    protected List<Map.Entry<HttpMethod, List<String>>> getEndpointsToIgnore() {
        return Collections.emptyList();
    }

    private static final String[] SWAGGER_WHITELIST = {
            "/v2/api-docs",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui.html",
            "/webjars/**"
    };
}