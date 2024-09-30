package com.example.authmanagement.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class WrongCredentialsException extends BadCredentialsException {

    public WrongCredentialsException(final String message) {
        super(message);
    }

    public WrongCredentialsException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
