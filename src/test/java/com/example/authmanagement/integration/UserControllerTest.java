package com.example.authmanagement.integration;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.authmanagement.auth.AuthService;
import com.example.authmanagement.auth.JWTUtil;
import com.example.authmanagement.auth.LoginResponseDto;
import com.example.authmanagement.config.PostgreSQLContainerConfig;
import com.example.authmanagement.enums.Role;
import com.example.authmanagement.user.User;
import com.example.authmanagement.user.UserDto;
import com.example.authmanagement.user.UserRepository;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest extends PostgreSQLContainerConfig {

    @LocalServerPort
    private int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();
    private final UserRepository userRepository;
    private final AuthService authService;
    private JWTUtil jwtUtil;

    @Autowired
    public UserControllerTest(final UserRepository userRepository, final AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    private UserDto userDto;
    private UserDto adminDto;
    private UserDto superadminDto;
    private UserDto incompleteUserDto;
    private UserDto userWithWrongCredentialsDto;

    @Transactional
    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
        userDto = createUserDto("user", "user", "user@nomail.com", Set.of(Role.USER));
        adminDto = createUserDto("admin", "admin", "admin@nomail.com", Set.of(Role.USER, Role.ADMIN));
        superadminDto = createUserDto("superadmin", "superadmin", "superadmin@nomail.com", Set.of(Role.USER, Role.ADMIN, Role.SUPER_ADMIN));
        incompleteUserDto = createUserDto("", "incomplete", "incomplete@nomail.com", null);
    }

    @Test
    public void signUpShouldReturnCorrectHttpStatus() {

        try {
//            ResponseEntity<Void> responseUserDto = restTemplate.postForEntity(
//                    createUrl("/signup"),
//                    userDto,
//                    Void.class);
//            assertEquals(HttpStatus.OK, responseUserDto.getStatusCode());

            ResponseEntity<Void> responseIncompleteUser = restTemplate.postForEntity(
                    createUrl("/signup"),
                    incompleteUserDto,
                    Void.class);
            assertEquals(HttpStatus.BAD_REQUEST, responseIncompleteUser.getStatusCode());
        } catch (RestClientException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void signUpShouldSaveNewUserInDatabase() {
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(
                    createUrl("/signup"),
                    userDto,
                    Void.class);
            ResponseEntity<Void> responseIncompleteUser = restTemplate.postForEntity(
                    createUrl("/signup"),
                    incompleteUserDto,
                    Void.class);

            User user = getUserByUserDto(userDto);
            User incompleteUser = getUserByUserDto(incompleteUserDto);

            assertNotNull(user);
            assertNull(incompleteUser);
        } catch (RestClientException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void signInShouldReturnCorrectHttpStatus() {
        try {
            if (!userRepository.existsByUsername(userDto.username())) {
                createUser(userDto);
            }

            if (!userRepository.existsByUsername(adminDto.username())) {
                createUser(adminDto);
            }
            adminDto = createUserDto(adminDto.username(), "wrongpassword", adminDto.email(), adminDto.roles());

            ResponseEntity<LoginResponseDto> userResponse = restTemplate.exchange(
                    createUrl("/signin"),
                    HttpMethod.PATCH,
                    requestWithPatchMethod(userDto),
                    LoginResponseDto.class);

            ResponseEntity<LoginResponseDto> adminResponse = restTemplate.exchange(
                    createUrl("/signin"),
                    HttpMethod.PATCH,
                    requestWithPatchMethod(adminDto),
                    LoginResponseDto.class);

            assertEquals(HttpStatus.OK, userResponse.getStatusCode());
            assertEquals(HttpStatus.OK, adminResponse.getStatusCode());
        } catch (RestClientException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void signInShouldReturnTokens() {
        try {
            if (!userRepository.existsByUsername(userDto.username())) {
                createUser(userDto);
            }

            ResponseEntity<LoginResponseDto> response = restTemplate.exchange(
                    createUrl("/signin"),
                    HttpMethod.PATCH,
                    requestWithPatchMethod(userDto),
                    LoginResponseDto.class);

            User user = getUserByUserDto(userDto);

            assertNotNull(response.getBody().accessToken());
            assertNotNull(response.getBody().refreshToken());
            assertTrue(user.getTokenExpirationDate().isAfter(LocalDateTime.now()));
            assertTrue(user.getRefreshTokenExpirationDate().isAfter(LocalDateTime.now()));
        } catch (RestClientException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void signInShouldReturnedTokensBeValid() {
        try {
            if (!userRepository.existsByUsername(userDto.username())) {
                createUser(userDto);
            }

            ResponseEntity<LoginResponseDto> response = restTemplate.exchange(
                    createUrl("/signin"),
                    HttpMethod.PATCH,
                    requestWithPatchMethod(userDto),
                    LoginResponseDto.class);

            User user = getUserByUserDto(userDto);
            DecodedJWT decodedAccessToken = jwtUtil.verifyToken(response.getBody().accessToken());
            DecodedJWT decodedRefreshToken = jwtUtil.verifyToken(response.getBody().refreshToken());

            assertEquals(response.getStatusCode(), HttpStatus.OK);
            assertNotNull(response.getBody().accessToken());
            assertNotNull(response.getBody().refreshToken());
//            assertTrue(response.getBody().TokenExpirationDate.isAfter(LocalDateTime.now()));
            assertTrue(user.getRefreshTokenExpirationDate().isAfter(LocalDateTime.now()));
        } catch (RestClientException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void signInShouldUpdateTokensInDatabase() {
        try {
            if (!userRepository.existsByUsername(userDto.username())) {
                createUser(userDto);
            }

            ResponseEntity<LoginResponseDto> response = restTemplate.exchange(
                    createUrl("/signin"),
                    HttpMethod.PATCH,
                    requestWithPatchMethod(userDto),
                    LoginResponseDto.class);

            User user = getUserByUserDto(userDto);

            assertEquals(response.getStatusCode(), HttpStatus.OK);
            assertNotNull(user.getToken());
            assertNotNull(user.getRefreshToken());
            assertTrue(user.getTokenExpirationDate().isAfter(LocalDateTime.now()));
            assertTrue(user.getRefreshTokenExpirationDate().isAfter(LocalDateTime.now()));
        } catch (RestClientException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    private HttpEntity<UserDto> requestWithPatchMethod(UserDto userDto) {
        var httpClient = HttpClientBuilder.create().build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        restTemplate.getRestTemplate().setRequestFactory(requestFactory);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(userDto, httpHeaders);
    }

    private UserDto createUserDto(String username, String password, String email, Set<Role> roles) {
        return new UserDto(null, username, password, email, false, roles, null, null, null, null);
    }

    @Transactional
    private void createUser(UserDto userDto) {
        User user = new User();
        user.setUsername(userDto.username());
        user.setPassword(authService.encryptPassword(userDto.password()));
        user.setEmail(userDto.email());
        user.setActive(true);
        user.setRoles(userDto.roles());
        userRepository.save(user);
    }

    private String createUrl(String URI) {
        return "http://localhost:" + port + URI;
    }

    private User getUserByUserDto(UserDto userDto) {
        return userRepository.findByUsername(userDto.username());
    }
}
