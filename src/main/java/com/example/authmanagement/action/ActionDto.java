package com.example.authmanagement.action;

public record ActionDto(
        Long id,
        String operation,
        String resource
) {
}
