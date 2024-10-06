package com.example.authmanagement.user;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.authmanagement.auth.AuthService;
import com.example.authmanagement.auth.LoginResponseDto;
import com.example.authmanagement.exceptions.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AuthService authService;

    public UserService(final UserRepository userRepository, final AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    private List<User> getUsers() {
        return userRepository.findAllByOrderByIdAsc();
    }

    private UserDto convertUserToUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.isActive(),
                user.getRoles(),
                user.getAccessToken(),
                user.getAccessTokenExpirationDate(),
                user.getRefreshToken(),
                user.getRefreshTokenExpirationDate());
    }

    public UserDto getUserDtoByUsername(String username) {
        return convertUserToUserDto(getUserByUsername(username));
    }

    public List<UserDto> getUserDtoList() {
        return getUsers().stream()
                .map(this::convertUserToUserDto)
                .collect(Collectors.toList());
    }


    public void signUp(UserDto userDto) {
        User user = new User();
        if (userDto.username().isEmpty()
                || userDto.password().isEmpty()
                || userDto.email().isEmpty()) {
            throw new EmptyDataException("Data can't be empty");
        } else if (userRepository.existsByEmail(userDto.email())) {
            throw new DataAlreadyExistsException("Email already exists");
        } else if (userRepository.existsByUsername(userDto.username())) {
            throw new DataAlreadyExistsException("Username already exists");
        }

        user.setUsername(userDto.username());
        user.setPassword(authService.encryptPassword(userDto.password()));
        user.setEmail(userDto.email());
        user.setActive(false);
        user.setRoles(userDto.roles());

        userRepository.save(user);
    }

    public LoginResponseDto signIn(UserDto userDto) {
        if (userDto.username().isBlank() || userDto.password().isBlank()) {
            throw new EmptyDataException("Data can't be empty");
        }
        UserDto encodedUserDto = getUserDtoByUsername(userDto.username());
        if (!encodedUserDto.isActive()) {
            throw new InactiveUserException("Account inactive");
        }
        if (authService.checkPassword(userDto, encodedUserDto)) {
            User user = getUserByUsername(userDto.username());
            String accessToken = authService.generateAccessToken(encodedUserDto);
            String refreshToken = authService.generateRefreshToken(encodedUserDto);
            user.setAccessToken(accessToken);
            user.setAccessTokenExpirationDate(authService.getTokenExpirationDate(accessToken));
            user.setRefreshToken(refreshToken);
            user.setRefreshTokenExpirationDate(authService.getTokenExpirationDate(refreshToken));
            userRepository.save(user);
            return new LoginResponseDto(accessToken, refreshToken);
        } else {
            throw new WrongCredentialsException("Wrong username or password");
        }
    }

    public LoginResponseDto refreshAccessToken(String bearerToken) {
        String refreshToken;
        DecodedJWT decodedJWT;
        User user;
        if (bearerToken.startsWith("Bearer ")) {
            refreshToken = bearerToken.substring(7);
            try {
                decodedJWT = authService.verifyToken(refreshToken);
            } catch (TokenExpiredException e) {
                throw new JwtTokenExpiredException(e.getMessage());
            }
            user = getUserByUsername(decodedJWT.getSubject());
        } else throw new BearerTokenNotProvidedException("Bearer token not provided");

        if (!user.getRefreshToken().equals(refreshToken)) {
            throw new WrongRefreshTokenException("Refresh token doesn't match");
        } else {
            String newAccessToken = authService.generateAccessToken(convertUserToUserDto(user));
            user.setAccessToken(newAccessToken);
            user.setAccessTokenExpirationDate(authService.getTokenExpirationDate(newAccessToken));
            userRepository.save(user);
            return new LoginResponseDto(newAccessToken, refreshToken);
        }
    }

    @Transactional
    public void activateAccount(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User with id=" + id + " doesn't exist"));
        if (!user.isActive()) {
            user.setActive(true);
            userRepository.save(user);
        }
    }

    @Transactional
    public void updateRoles(UserDto userDto) {
        if (userDto.username() == null || userDto.roles() == null) {
            throw new EmptyDataException("Username and set of roles required");
        }

        User user = getUserByUsername(userDto.username());
        user.setRoles(userDto.roles());
        userRepository.save(user);
    }
}
