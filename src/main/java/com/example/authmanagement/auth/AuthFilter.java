package com.example.authmanagement.auth;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.authmanagement.enums.Operation;
import com.example.authmanagement.enums.Resource;
import com.example.authmanagement.enums.Role;
import com.example.authmanagement.exceptions.NoAccessException;
import com.example.authmanagement.exceptions.SessionExpiredException;
import com.example.authmanagement.exceptions.UserNotFoundException;
import com.example.authmanagement.user.UserRepository;
import com.example.authmanagement.user.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

@Component
public class AuthFilter extends OncePerRequestFilter {

    private JWTUtil jwtUtil;
    private UserRepository userRepository;

    public AuthFilter(final JWTUtil jwtUtil, final UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Transactional
    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            DecodedJWT decodedJWT = jwtUtil.verifyToken(token.replace("Bearer ", ""));

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
        }
        filterChain.doFilter(request, response);
    }

    private boolean hasAccessTo(Long id, Operation operation, Resource resource) {
        for (Role role : userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found.")).getRoles()) {
            if (role.hasAccessTo(operation, resource)) {
                return true;
            }
        }
        return false;
    }
}
