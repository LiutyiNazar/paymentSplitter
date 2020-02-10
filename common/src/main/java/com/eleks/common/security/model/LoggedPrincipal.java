package com.eleks.common.security.model;

import lombok.Getter;

@Getter
public class LoggedPrincipal extends JwtUserDataClaim {
    private String jwt;

    public LoggedPrincipal(String username, Long userId, String jwt) {
        super(username, userId);
        this.jwt = jwt;
    }
}
