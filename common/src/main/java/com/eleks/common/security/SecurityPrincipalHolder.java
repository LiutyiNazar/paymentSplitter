package com.eleks.common.security;

import com.eleks.common.security.model.LoggedPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityPrincipalHolder {

    public void setPrincipal(LoggedPrincipal principal) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public LoggedPrincipal getPrincipal() {
        return (LoggedPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
