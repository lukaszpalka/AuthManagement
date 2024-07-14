package com.example.authmanagement.auth;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.authmanagement.enums.Operation;
import com.example.authmanagement.enums.Resource;
import com.example.authmanagement.enums.Role;
import com.example.authmanagement.exceptions.UserNotFoundException;
import com.example.authmanagement.user.UserDto;
import com.example.authmanagement.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final JWTUtil jwtUtil;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserRepository userRepository;

    @Autowired
    public AuthService(final JWTUtil jwtUtil, final BCryptPasswordEncoder bCryptPasswordEncoder, final UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userRepository = userRepository;
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

    public boolean hasAccessTo(Long id, Operation operation, Resource resource) {
        for (Role role : userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found.")).getRoles()) {
            if (role.hasAccessTo(operation, resource)) {
                return true;
            }
        }
        return false;
    }

    public LocalDateTime getTokenExpirationDate(String token) {
        return jwtUtil.getExpirationDate(verifyToken(token));
    }

    public DecodedJWT verifyToken(String refreshToken) {
        return jwtUtil.verifyToken(refreshToken);
    }
}
