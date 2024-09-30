package com.example.authmanagement.auth;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.authmanagement.exceptions.SessionExpiredException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

@Component
public class AuthFilterDelegate {

    private final JWTUtil jwtUtil;

    public AuthFilterDelegate(final JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {
        Optional<String> token = Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .map(authHeader -> authHeader.substring("Bearer ".length()));
        if (token.isEmpty() || request.getRequestURI().equals("/refreshtoken")) {
            filterChain.doFilter(request, response);
            return;
        }

        DecodedJWT decodedJWT = jwtUtil.verifyToken(token.get());
        if (decodedJWT.getExpiresAt().before(Date.from(Instant.now()))) {
            throw new SessionExpiredException("Session expired");
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(null, null, Collections.singleton(new SimpleGrantedAuthority("user")));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
