package com.example.authmanagement.user;

import com.example.authmanagement.enums.Role;
import lombok.Data;
import org.springframework.boot.jackson.JsonComponent;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

public record UserDto(
        Long id,
        String username,
        String password,
        String email,
        boolean isActive,
        Set<Role> roles,
        String token,
        LocalDateTime tokenExpirationDate) {
//        String refreshToken,
//        LocalDateTime refreshTokenExpirationDate) {

}

