package com.example.authmanagement.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class NoAccessException extends RuntimeException {
    public NoAccessException(final String message) {
        super(message);
        System.out.println(super.toString());
    }
}
