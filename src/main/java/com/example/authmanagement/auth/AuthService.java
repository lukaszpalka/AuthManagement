package com.example.authmanagement.auth;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.authmanagement.user.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final JWTUtil jwtUtil;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public AuthService(final JWTUtil jwtUtil, final BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.jwtUtil = jwtUtil;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public String encryptPassword(String password) {
        return bCryptPasswordEncoder.encode(password);
    }

    public boolean checkPassword(UserDto userDto, UserDto encodedUserDto) {
        if (userDto.username().isBlank() || userDto.password().isBlank()) {
            throw new IllegalArgumentException("Data can't be empty");
        }
        return bCryptPasswordEncoder.matches(userDto.password(), encodedUserDto.password());
    }

    public String generateJwtToken(UserDto userDto) {
        return jwtUtil.createToken(userDto);
    }

    public String generateRefreshToken(UserDto userDto) {
        return jwtUtil.createRefreshToken(userDto);
    }

    public LocalDateTime getTokenExpirationDate(String token) {
        return jwtUtil.getExpirationDate(verifyToken(token));
    }

    public LocalDateTime getRefreshTokenExpirationDate(String refreshToken) {
        return jwtUtil.getExpirationDate(verifyToken(refreshToken));
    }

    public DecodedJWT verifyToken(String refreshToken) {
        return jwtUtil.verifyToken(refreshToken);
    }
}
