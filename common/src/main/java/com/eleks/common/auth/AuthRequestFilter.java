package com.eleks.common.auth;


import com.eleks.common.security.JwtTokenService;
import com.eleks.common.security.SecurityPrincipalHolder;
import com.eleks.common.security.model.JwtUserDataClaim;
import com.eleks.common.security.model.LoggedPrincipal;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.eleks.common.config.SecurityConstants.AUTH_HEADER;
import static com.eleks.common.config.SecurityConstants.BEARER_TOKEN_PREFIX;
import static java.util.Objects.nonNull;

@Slf4j
public class AuthRequestFilter extends OncePerRequestFilter {

    private JwtTokenService jwtTokenService;
    private SecurityPrincipalHolder principalHolder;

    public AuthRequestFilter(JwtTokenService jwtTokenService, SecurityPrincipalHolder principalHolder) {
        this.jwtTokenService = jwtTokenService;
        this.principalHolder = principalHolder;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String requestHeader = request.getHeader(AUTH_HEADER);
        String token = getTokenFromHeader(requestHeader);
        JwtUserDataClaim userClaim = getUserFromJwtToken(token);
        if (nonNull(userClaim)) {
            LoggedPrincipal principal = new LoggedPrincipal(userClaim.getUsername(), userClaim.getUserId(), token);
            principalHolder.setPrincipal(principal);
        }

        chain.doFilter(request, response);
    }

    private String getTokenFromHeader(String requestTokenHeader) {
        if (nonNull(requestTokenHeader) && requestTokenHeader.startsWith(BEARER_TOKEN_PREFIX)) {
            return requestTokenHeader.replace(BEARER_TOKEN_PREFIX, "");
        }
        log.warn("Token is missing.");
        return null;

    }

    private JwtUserDataClaim getUserFromJwtToken(String jwtToken) {
        try {
            return jwtTokenService.getUserFromToken(jwtToken);
        } catch (ExpiredJwtException exception) {
            log.warn("Request to parse expired JWT : {} failed : {}", jwtToken, exception.getMessage());
        } catch (UnsupportedJwtException exception) {
            log.warn("Request to parse unsupported JWT : {} failed : {}", jwtToken, exception.getMessage());
        } catch (MalformedJwtException exception) {
            log.warn("Request to parse invalid JWT : {} failed : {}", jwtToken, exception.getMessage());
        } catch (SignatureException exception) {
            log.warn("Request to parse JWT with invalid signature : {} failed : {}", jwtToken, exception.getMessage());
        } catch (IllegalArgumentException exception) {
            log.warn("Request to parse empty or null JWT : {} failed : {}", jwtToken, exception.getMessage());
        } catch (IOException exception) {
            log.warn("JWT parsing exception: {} failed : {}", jwtToken, exception.getMessage());
        }
        return null;
    }
}