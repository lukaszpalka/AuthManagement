package com.example.authmanagement.auth;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.authmanagement.enums.Operation;
import com.example.authmanagement.enums.Resource;
import com.example.authmanagement.exceptions.NoAccessException;
import com.example.authmanagement.exceptions.SessionExpiredException;
import com.example.authmanagement.exceptions.UserNotFoundException;
import com.example.authmanagement.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Component
public class AuthFilterDelegate {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;

    public AuthFilterDelegate(final JWTUtil jwtUtil, final UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Transactional
    public void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {
        Optional<String> token = Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .map(authHeader -> authHeader.substring("Bearer ".length()));
        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        DecodedJWT decodedJWT = jwtUtil.verifyToken(token.get());
        if (decodedJWT.getExpiresAt().before(Date.from(Instant.now()))) {
            throw new SessionExpiredException("Session expired");
        }

        String myOperation = request.getParameter("operation");
        String myResource = request.getParameter("resource");
        Operation operation = Operation.valueOf(myOperation);
        Resource resource = Resource.valueOf(myResource);

        if (!hasAccessTo(Long.valueOf(decodedJWT.getClaim("id").toString()), operation, resource)) {
            throw new NoAccessException("No access");
        }
        filterChain.doFilter(request, response);
    }


    private boolean hasAccessTo(Long id, Operation operation, Resource resource) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found."))
                .getRoles()
                .stream()
                .anyMatch(role -> role.hasAccessTo(operation, resource));
    }
}
