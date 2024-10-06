package com.example.authmanagement.unit;

import com.example.authmanagement.auth.AuthService;
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
import org.springframework.transaction.annotation.Transactional;

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
        User user = saveAndGetUser(userDto);
//        UserDto userDtoWithIdAssigned = assignIdToUserDto(userDto, user.getId());
        UserDto encodedUserDto = convertUserToUserDto(user);
        LocalDateTime expAccessToken = LocalDateTime.now().plusHours(1);
        LocalDateTime expRefreshToken = LocalDateTime.now().plusDays(30);
        when(authService.checkPassword(userDto, encodedUserDto)).thenReturn(true);
        when(authService.generateAccessToken(encodedUserDto)).thenReturn("accessToken");
        when(authService.generateRefreshToken(encodedUserDto)).thenReturn("refreshToken");
        when(authService.getTokenExpirationDate("accessToken")).thenReturn(expAccessToken);
        when(authService.getTokenExpirationDate("refreshToken")).thenReturn(expRefreshToken);

        userService.signIn(encodedUserDto);

        user = userRepository.findByUsername(userDto.username());

        assertNotNull(user);
        assertEquals("accessToken", user.getAccessToken());
        assertEquals("refreshToken", user.getRefreshToken());
        assertEquals(expAccessToken, user.getAccessTokenExpirationDate());
        assertEquals(expRefreshToken, user.getRefreshTokenExpirationDate());
    }



//    -------------------------------------------------------------------

    private User getUser() {
        Set<Role> roles = Set.of(Role.USER);
        return new User(1L,
                "user",
                "user",
                "user@nomail.com",
                true,
                roles,
                "accessToken",
                LocalDateTime.now(),
                "refreshToken",
                LocalDateTime.now());
    }

    @Transactional
    private User saveAndGetUser(UserDto userDto) {
        User user = new User();
        user.setUsername(userDto.username());
        user.setPassword(authService.encryptPassword(userDto.password()));
        user.setEmail(userDto.email());
        user.setActive(true);
        user.setRoles(userDto.roles());
        userRepository.save(user);
        return userRepository.findByUsername(user.getUsername());
    }

    private UserDto assignIdToUserDto(UserDto userDto, Long id) {
        return new UserDto(id, userDto.username(), userDto.password(), userDto.email(), false, userDto.roles(), null, null, null, null);

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
