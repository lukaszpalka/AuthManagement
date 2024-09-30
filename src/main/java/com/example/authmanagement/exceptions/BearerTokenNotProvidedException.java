package com.example.authmanagement.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BearerTokenNotProvidedException extends RuntimeException {
    public BearerTokenNotProvidedException(final String message) {
        super(message);
    }
}
