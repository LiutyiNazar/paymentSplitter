package com.eleks.common.security;

import com.eleks.common.security.model.JwtUserDataClaim;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenUtil implements Serializable {
    public static final long JWT_VALIDITY_TIME_MILLIS = 60 * 60 * 1000;

    private String secret;
    private ObjectMapper objectMapper;

    public JwtTokenUtil(@Value("${jwt.secret}") String secret, ObjectMapper objectMapper) {
        this.secret = secret;
        this.objectMapper = objectMapper;
    }

    public JwtUserDataClaim getUserFromToken(String token) throws IOException {
        String rawData = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getSubject();
        return objectMapper.readValue(rawData, JwtUserDataClaim.class);
    }

    public String generateToken(JwtUserDataClaim userDataClaim) throws IOException {
        return doGenerateToken(objectMapper.writeValueAsString(userDataClaim), JWT_VALIDITY_TIME_MILLIS);
    }

    public String doGenerateToken(String subject, long validityTimeMillis) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + validityTimeMillis))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }
}
