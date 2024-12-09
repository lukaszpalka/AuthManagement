package com.example.authmanagement.unit;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.authmanagement.auth.AuthService;
import com.example.authmanagement.auth.LoginResponseDto;
import com.example.authmanagement.enums.Role;
import com.example.authmanagement.exceptions.DataAlreadyExistsException;
import com.example.authmanagement.exceptions.EmptyDataException;
import com.example.authmanagement.user.User;
import com.example.authmanagement.user.UserDto;
import com.example.authmanagement.user.UserRepository;
import com.example.authmanagement.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthService authService;

    @Mock
    private DecodedJWT decodedJWT;

    @InjectMocks
    private UserService userService;

    private UserDto userDto;
    private UserDto adminDto;
    private UserDto incompleteUserDto;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
        userDto = getUserDto("user", "user", "user@nomail.com", Set.of(Role.USER));
        adminDto = getUserDto("admin", "admin", "admin@nomail.com", Set.of(Role.USER, Role.ADMIN));
        incompleteUserDto = getUserDto("", "incomplete", "incomplete@nomail.com", null);
    }

    @Test
    public void GetUserDtoByUsernameShouldReturnCorrectDto() {
        when(userRepository.findByUsername("user")).thenReturn(getUser());

        UserDto userDto = userService.getUserDtoByUsername("user");

        assertNotNull(userDto);
        assertEquals("user", userDto.username());
    }

    @Test
    public void SignUpThrowEmptyDataShouldExceptionWhenDataEmpty() {
        assertThrows(EmptyDataException.class, () -> userService.signUp(incompleteUserDto));
    }

    @Test
    public void SignUpShouldThrowDataAlreadyExistsExceptionWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail(userDto.email())).thenReturn(true);

        assertThrows(DataAlreadyExistsException.class, () -> userService.signUp(userDto));
    }

    @Test
    public void SignUpShouldThrowDataAlreadyExistsExceptionWhenUsernameAlreadyExists() {
        when(userRepository.existsByUsername(userDto.username())).thenReturn(true);

        assertThrows(DataAlreadyExistsException.class, () -> userService.signUp(userDto));
    }

    @Test
    public void SignUpShouldSaveNewUserToDatabase() {
        when(userRepository.existsByUsername(userDto.username())).thenReturn(false);
        when(userRepository.existsByEmail(userDto.email())).thenReturn(false);
        when(authService.encryptPassword(userDto.password())).thenReturn("user");

        userService.signUp(userDto);

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userArgumentCaptor.capture());

        User savedUser = userArgumentCaptor.getValue();
        UserDto savedUserDto = convertUserToUserDto(savedUser);

        assertNotNull(savedUser);
        assertEquals(userDto, savedUserDto);
    }

    @Test
    public void SignInShouldAssignTokensToUserAndSaveToDatabase() {
        LocalDateTime expAccessToken = LocalDateTime.now().plusHours(1);
        LocalDateTime expRefreshToken = LocalDateTime.now().plusHours(30);
        UserDto userCredentialsDto = new UserDto(null, userDto.username(), userDto.password(), null, false, null, null, null, null, null);

        when(userRepository.findByUsername(userDto.username())).thenReturn(getUser());
        User registeredUser = userRepository.findByUsername(userDto.username());
        UserDto registeredUserDto = convertUserToUserDto(registeredUser);

        when(userService.getUserByUsername(userCredentialsDto.username())).thenReturn(registeredUser);
        when(authService.checkPassword(userCredentialsDto, registeredUserDto)).thenReturn(true);
        when(authService.generateAccessToken(registeredUserDto)).thenReturn("accessToken");
        when(authService.generateRefreshToken(registeredUserDto)).thenReturn("refreshToken");
        when(authService.getTokenExpirationDate("accessToken")).thenReturn(expAccessToken);
        when(authService.getTokenExpirationDate("refreshToken")).thenReturn(expRefreshToken);

        userService.signIn(userCredentialsDto);

        User loggedUser = userRepository.findByUsername(userCredentialsDto.username());

        assertNotNull(loggedUser);
        assertEquals("accessToken", loggedUser.getAccessToken());
        assertEquals("refreshToken", loggedUser.getRefreshToken());
        assertEquals(expAccessToken, loggedUser.getAccessTokenExpirationDate());
        assertEquals(expRefreshToken, loggedUser.getRefreshTokenExpirationDate());
    }

    @Test
    public void refreshAccessTokenShouldReturnNewToken() {
        User user = getUser();
        updateTokensParams(user);
        String oldAccessToken = user.getAccessToken();

        when(authService.verifyToken(user.getRefreshToken())).thenReturn(decodedJWT);
        when(decodedJWT.getSubject()).thenReturn(user.getUsername());
        when(userService.getUserByUsername(user.getUsername())).thenReturn(user);
        when(authService.generateAccessToken(convertUserToUserDto(user))).thenReturn("newAccessToken");
        when(authService.getTokenExpirationDate("newAccessToken")).thenReturn(LocalDateTime.now().plusHours(1));

        LoginResponseDto loginResponseDto = userService.refreshAccessToken("Bearer " + user.getRefreshToken());

        assertNotNull(loginResponseDto);
        assertEquals("newAccessToken", loginResponseDto.accessToken());
        assertNotEquals(oldAccessToken, loginResponseDto.accessToken());
        assertEquals("refreshToken", loginResponseDto.refreshToken());
        assertEquals(user.getRefreshToken(), loginResponseDto.refreshToken());
    }

//    -------------------------------------------------------------------

    private User getUser() {
        return new User(1L,
                userDto.username(),
                userDto.password(),
                userDto.email(),
                true,
                userDto.roles(),
                null,
                null,
                null,
                null);
    }

    private void updateTokensParams(User user) {
        user.setAccessToken("accessToken");
        user.setAccessTokenExpirationDate(LocalDateTime.now().plusHours(1));
        user.setRefreshToken("refreshToken");
        user.setAccessTokenExpirationDate(LocalDateTime.now().plusHours(30));
    }

    private UserDto getUserDto(String username, String password, String email, Set<Role> roles) {
        return new UserDto(null, username, password, email, false, roles, null, null, null, null);
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
}
