package com.eleks.common.security.model;

import lombok.Getter;

@Getter
public class LoggedInUserPrincipal extends JwtUserDataClaim {
    private String jwt;

    public LoggedInUserPrincipal(String username, Long userId, String jwt) {
        super(username, userId);
        this.jwt = jwt;
    }
}
