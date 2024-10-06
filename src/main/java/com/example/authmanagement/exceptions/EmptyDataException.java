package com.example.authmanagement.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class EmptyDataException extends RuntimeException {
    public EmptyDataException(final String message) {
        super(message);
        System.out.println(super.toString());
    }
}
