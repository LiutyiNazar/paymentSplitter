package com.eleks.common.security;

import com.eleks.common.security.model.LoggedInUserPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityPrincipalHolder {

    public void setPrincipal(LoggedInUserPrincipal principal) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public LoggedInUserPrincipal getPrincipal() {
        return (LoggedInUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
