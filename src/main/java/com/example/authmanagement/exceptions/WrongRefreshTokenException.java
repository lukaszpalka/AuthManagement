package com.example.authmanagement.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class WrongRefreshTokenException extends RuntimeException {
    public WrongRefreshTokenException(final String message) {
        super(message);
        System.out.println(super.toString());
    }
}
