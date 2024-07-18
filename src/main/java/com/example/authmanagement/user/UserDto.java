package com.example.authmanagement.user;

import com.example.authmanagement.enums.Role;

import java.time.LocalDateTime;
import java.util.Set;

public record UserDto(
        Long id,
        String username,
        String password,
        String email,
        boolean isActive,
        Set<Role> roles,
        String token,
        LocalDateTime tokenExpirationDate,
        String refreshToken,
        LocalDateTime refreshTokenExpirationDate) {

}

