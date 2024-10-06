package com.example.authmanagement.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class JwtTokenExpiredException extends RuntimeException {
    public JwtTokenExpiredException(final String message) {
        super(message);
        System.out.println(super.toString());
    }
}
