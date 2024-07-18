package com.example.authmanagement.user;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.authmanagement.auth.AuthService;
import com.example.authmanagement.enums.Role;
import com.example.authmanagement.exceptions.*;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
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
                user.getToken(),
                user.getTokenExpirationDate(),
                user.getRefreshToken(),
                user.getRefreshTokenExpirationDate());
    }

    public UserDto getUserDtoByUsername(String username) {
        return convertUserToUserDto(getUserByUsername(username));
    }

    public List<UserDto> getUserDtos() {
        return getUsers().stream()
                .map(this::convertUserToUserDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void signUp(UserDto userDto) {
        User user = new User();
        if (userDto.username().isBlank()
                || userDto.password().isBlank()
                || userDto.email().isBlank()) {
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

//    @Transactional
//    public String signIn(UserDto userDto) {
//        if (userDto.username().isBlank() || userDto.password().isBlank()) {
//            throw new EmptyDataException("Data can't be empty");
//        }
//        UserDto encodedUserDto = getUserDtoByUsername(userDto.username());
//        if (authService.checkPassword(userDto, encodedUserDto) && isActive(encodedUserDto)) {
//            User user = getUserByUsername(userDto.username());
//            String accessToken = authService.generateJwtToken(encodedUserDto);
//            user.setToken(accessToken);
//            user.setTokenExpirationDate(authService.getTokenExpirationDate(accessToken));
//            userRepository.save(user);
//            return accessToken;
//        } else {
//            throw new BadCredentialsException("Wrong username or password");
//        }
//    }


//    -------------------------------- with refresh token ----------------------------------

    @Transactional
    public String signIn(UserDto userDto) {
        if (userDto.username().isBlank() || userDto.password().isBlank()) {
            throw new EmptyDataException("Data can't be empty");
        }
        UserDto encodedUserDto = getUserDtoByUsername(userDto.username());
        if (authService.checkPassword(userDto, encodedUserDto) && isActive(encodedUserDto)) {
            User user = getUserByUsername(userDto.username());
            String accessToken = authService.generateJwtToken(encodedUserDto);
            String refreshToken = authService.generateRefreshToken(encodedUserDto);
            user.setToken(accessToken);
            user.setTokenExpirationDate(authService.getTokenExpirationDate(accessToken));
            user.setRefreshToken(refreshToken);
            user.setRefreshTokenExpirationDate(authService.getRefreshTokenExpirationDate(refreshToken));
            userRepository.save(user);
            return accessToken;
        } else {
            throw new BadCredentialsException("Wrong username or password");
        }
    }

    @Transactional
    public String refreshAccessToken(String refreshBearerToken) {
        if (!refreshBearerToken.startsWith("Bearer ")) {
            throw new WrongRefreshTokenException("Bearer token not provided");
        }

        String refreshToken = refreshBearerToken.replace("Bearer ", "");

        DecodedJWT decodedJWT = authService.verifyToken(refreshToken);
        User user = getUserByUsername(decodedJWT.getSubject());

        if (!user.getRefreshToken().equals(refreshToken)) {
            throw new WrongRefreshTokenException("Refresh token doesn't match");
        } else if (user.getRefreshTokenExpirationDate().isBefore(LocalDateTime.now())) {
            throw new RefreshTokenExpiredException("Refresh token expired");
        } else {
            String newAccessToken = authService.generateJwtToken(convertUserToUserDto(user));
            user.setToken(newAccessToken);
            user.setTokenExpirationDate(authService.getTokenExpirationDate(newAccessToken));
            userRepository.save(user);
            return newAccessToken;
        }
    }

    @Transactional
    public void activateAccount(UserDto userDto) {
        User user = getUserByUsername(userDto.username());
        if (!user.isActive()) {
            user.setActive(true);
            userRepository.save(user);
        }
    }

    @Transactional
    public void deactivateAccount(UserDto userDto) {
        User user = getUserByUsername(userDto.username());
        if (user.isActive()) {
            user.setActive(false);
            userRepository.save(user);
        }
    }

    @Transactional
    public void updateRoles(UserDto userDto, Set<Role> roles) {
        User user = getUserByUsername(userDto.username());
        user.setRoles(roles);
        userRepository.save(user);
    }

    public void removeUser(Long id) {
        userRepository.deleteById(id);
    }

    private boolean isActive(UserDto userDto) {
        if (userDto.isActive()) {
            return true;
        } else {
            throw new InactiveUserException("Account inactive");
        }
    }
}
