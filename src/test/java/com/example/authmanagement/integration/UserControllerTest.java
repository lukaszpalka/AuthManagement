package com.example.authmanagement.integration;

import com.example.authmanagement.auth.AuthService;
import com.example.authmanagement.auth.JWTUtil;
import com.example.authmanagement.auth.LoginResponseDto;
import com.example.authmanagement.config.PostgreSQLContainerConfig;
import com.example.authmanagement.enums.Role;
import com.example.authmanagement.user.User;
import com.example.authmanagement.user.UserDto;
import com.example.authmanagement.user.UserRepository;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest extends PostgreSQLContainerConfig {

    @LocalServerPort
    private int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();
    private final UserRepository userRepository;
    private final AuthService authService;
    private final JWTUtil jwtUtil;

    @Autowired
    public UserControllerTest(final UserRepository userRepository, final AuthService authService, final JWTUtil jwtUtil) {
        this.userRepository = userRepository;
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    private UserDto userDto;
    private UserDto adminDto;
    private UserDto incompleteUserDto;

    @Transactional
    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
        userDto = createUserDto("user", "user", "user@nomail.com", Set.of(Role.USER));
        adminDto = createUserDto("admin", "admin", "admin@nomail.com", Set.of(Role.USER, Role.ADMIN));
        incompleteUserDto = createUserDto("", "incomplete", "incomplete@nomail.com", null);
    }

    @Test
    public void signUpShouldReturnOkWhenDataComplete() {
        try {
            ResponseEntity<Void> responseUserDto = restTemplate.postForEntity(
                    createUrl("/signup"),
                    userDto,
                    Void.class);

            assertEquals(HttpStatus.OK, responseUserDto.getStatusCode());
        } catch (RestClientException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void signUpShouldReturnBadRequestWhenDataIncomplete() {
        try {
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
    public void signUpShouldSaveNewUserInDatabaseWhenDataComplete() {
        try {
            ResponseEntity<Void> responseUser = restTemplate.postForEntity(
                    createUrl("/signup"),
                    userDto,
                    Void.class);

            User user = getUserByUserDto(userDto);

            assertNotNull(user);
        } catch (RestClientException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void signUpShouldNotSaveNewUserInDatabaseWhenDataIncomplete() {
        try {
            ResponseEntity<Void> responseIncompleteUser = restTemplate.postForEntity(
                    createUrl("/signup"),
                    incompleteUserDto,
                    Void.class);

            User incompleteUser = getUserByUserDto(incompleteUserDto);

            assertNull(incompleteUser);
        } catch (RestClientException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void signInShouldReturnOkWhenCredentialsCorrect() {
        try {
            createUser(userDto);

            ResponseEntity<LoginResponseDto> userResponse = restTemplate.exchange(
                    createUrl("/signin"),
                    HttpMethod.PATCH,
                    requestWithBodyForPatchMethod(userDto),
                    LoginResponseDto.class);

            assertEquals(HttpStatus.OK, userResponse.getStatusCode());
        } catch (RestClientException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void signInShouldReturnForbiddenWhenCredentialsIncorrect() {
        try {
            UserDto adminDtoWithWrongCredentials = getUserDtoWithWrongCredentials(adminDto);

            ResponseEntity<LoginResponseDto> myAdminResponse = restTemplate.exchange(
                    createUrl("/signin"),
                    HttpMethod.PATCH,
                    requestWithBodyForPatchMethod(adminDtoWithWrongCredentials),
                    LoginResponseDto.class);

            assertEquals(HttpStatus.FORBIDDEN, myAdminResponse.getStatusCode());
        } catch (RestClientException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void signInShouldReturnTokensWhenCredentialsCorrect() {
        try {
            createUser(userDto);

            ResponseEntity<LoginResponseDto> response = restTemplate.exchange(
                    createUrl("/signin"),
                    HttpMethod.PATCH,
                    requestWithBodyForPatchMethod(userDto),
                    LoginResponseDto.class);

            assertNotNull(response.getBody().accessToken());
            assertNotNull(response.getBody().refreshToken());
        } catch (RestClientException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void signInShouldNotReturnTokensWhenCredentialsIncorrect() {
        try {
            UserDto adminDtoWithWrongCredentials = getUserDtoWithWrongCredentials(adminDto);

            ResponseEntity<LoginResponseDto> myAdminResponse = restTemplate.exchange(
                    createUrl("/signin"),
                    HttpMethod.PATCH,
                    requestWithBodyForPatchMethod(adminDtoWithWrongCredentials),
                    LoginResponseDto.class);

            assertNull(myAdminResponse.getBody().accessToken());
            assertNull(myAdminResponse.getBody().refreshToken());
        } catch (RestClientException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void signInShouldReturnedTokensBeValid() {
        try {
            createUser(userDto);

            ResponseEntity<LoginResponseDto> response = restTemplate.exchange(
                    createUrl("/signin"),
                    HttpMethod.PATCH,
                    requestWithBodyForPatchMethod(userDto),
                    LoginResponseDto.class);

            assertTrue(jwtUtil.getExpirationDate(response.getBody().accessToken()).isAfter(LocalDateTime.now()));
            assertTrue(jwtUtil.getExpirationDate(response.getBody().refreshToken()).isAfter(LocalDateTime.now()));
        } catch (RestClientException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void signInShouldUpdateTokensInDatabaseWhenCredentialsCorrect() {
        try {
            createUser(userDto);

            ResponseEntity<LoginResponseDto> userResponse = restTemplate.exchange(
                    createUrl("/signin"),
                    HttpMethod.PATCH,
                    requestWithBodyForPatchMethod(userDto),
                    LoginResponseDto.class);

            User user = getUserByUserDto(userDto);

            assertEquals(userResponse.getBody().accessToken(), user.getAccessToken());
            assertEquals(userResponse.getBody().refreshToken(), user.getRefreshToken());
            assertEquals(jwtUtil.getExpirationDate(userResponse.getBody().accessToken()), user.getAccessTokenExpirationDate());
            assertEquals(jwtUtil.getExpirationDate(userResponse.getBody().refreshToken()), user.getRefreshTokenExpirationDate());
        } catch (RestClientException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void signInShouldNotUpdateTokensInDatabaseWhenCredentialsIncorrect() {
        try {
            UserDto adminDtoWithWrongCredentials = getUserDtoWithWrongCredentials(adminDto);

            ResponseEntity<LoginResponseDto> myAdminResponse = restTemplate.exchange(
                    createUrl("/signin"),
                    HttpMethod.PATCH,
                    requestWithBodyForPatchMethod(adminDtoWithWrongCredentials),
                    LoginResponseDto.class);

            User myAdmin = getUserByUserDto(adminDtoWithWrongCredentials);

            assertEquals(myAdminResponse.getBody().accessToken(), myAdmin.getAccessToken());
            assertEquals(myAdminResponse.getBody().refreshToken(), myAdmin.getRefreshToken());
            assertEquals(jwtUtil.getExpirationDate(myAdminResponse.getBody().accessToken()), myAdmin.getAccessTokenExpirationDate());
            assertEquals(jwtUtil.getExpirationDate(myAdminResponse.getBody().refreshToken()), myAdmin.getRefreshTokenExpirationDate());
        } catch (RestClientException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void refreshAccessTokenShouldReturnOkWhenRefreshTokenCorrect() {
        try {
            User user = createUser(userDto);
            String userDtoRefreshToken = authService.generateRefreshToken(userDto);
            String userDtoBearerRefreshToken = "Bearer ".concat(userDtoRefreshToken);

            updateRefreshTokenInDatabase(userDtoRefreshToken, user);

            ResponseEntity<LoginResponseDto> userResponse = restTemplate.exchange(
                    createUrl("/refreshtoken"),
                    HttpMethod.PATCH,
                    requestWithBearerTokenForPatchMethod(userDtoBearerRefreshToken),
                    LoginResponseDto.class);

            assertEquals(HttpStatus.OK, userResponse.getStatusCode());
        } catch (RestClientException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void refreshAccessTokenShouldReturnBadRequestWhenRefreshTokenIsNotBearer() {
        try {
            User user = createUser(userDto);
            String userDtoRefreshToken = authService.generateRefreshToken(userDto);
            String userDtoWrongRefreshToken = "wrongtoken";

            updateRefreshTokenInDatabase(userDtoRefreshToken, user);

            ResponseEntity<LoginResponseDto> userWithWrongRefreshTokenResponse = restTemplate.exchange(
                    createUrl("/refreshtoken"),
                    HttpMethod.PATCH,
                    requestWithBearerTokenForPatchMethod(userDtoWrongRefreshToken),
                    LoginResponseDto.class);

            assertEquals(HttpStatus.BAD_REQUEST, userWithWrongRefreshTokenResponse.getStatusCode());
        } catch (RestClientException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void refreshAccessTokenShouldReturnUnauthorizedWhenRefreshTokenDoesNotMatch() {
        try {
            User user = createUser(userDto);

            String userDtoRefreshToken = authService.generateRefreshToken(userDto);
            String adminDtoRefreshToken = authService.generateRefreshToken(adminDto);
            String userDtoWrongRefreshToken = "Bearer ".concat(userDtoRefreshToken);
            updateRefreshTokenInDatabase(adminDtoRefreshToken, user);

            ResponseEntity<LoginResponseDto> userWithWrongRefreshTokenResponse = restTemplate.exchange(
                    createUrl("/refreshtoken"),
                    HttpMethod.PATCH,
                    requestWithBearerTokenForPatchMethod(userDtoWrongRefreshToken),
                    LoginResponseDto.class);

            assertEquals(HttpStatus.UNAUTHORIZED, userWithWrongRefreshTokenResponse.getStatusCode());
        } catch (RestClientException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void refreshAccessTokenShouldReturnUnauthorizedWhenRefreshTokenOutOfDate() {
        try {
            Date expNow = Date.from(Instant.now());
            LocalDateTime expNowLocalDateTime = LocalDateTime.ofInstant(expNow.toInstant(), ZoneId.systemDefault());
            User user = createUser(userDto);

            String userDtoExpiredRefreshToken = authService.generateTokenWithCustomExpirationDate(userDto, expNow);
            updateRefreshTokenInDatabase(userDtoExpiredRefreshToken, user, expNowLocalDateTime);

            String userDtoExpiredBearerRefreshToken = "Bearer ".concat(userDtoExpiredRefreshToken);

            ResponseEntity<LoginResponseDto> userWithExpiredRefreshTokenResponse = restTemplate.exchange(
                    createUrl("/refreshtoken"),
                    HttpMethod.PATCH,
                    requestWithBearerTokenForPatchMethod(userDtoExpiredBearerRefreshToken),
                    LoginResponseDto.class);

            assertEquals(HttpStatus.UNAUTHORIZED, userWithExpiredRefreshTokenResponse.getStatusCode());
        } catch (RestClientException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void refreshAccessTokenShouldUpdateAccessTokenInDatabaseWhenRefreshTokenCorrect() {
        try {
            User user = createUser(userDto);
            String userDtoRefreshToken = authService.generateRefreshToken(userDto);
            updateRefreshTokenInDatabase(userDtoRefreshToken, user);
            String userDtoBearerRefreshToken = "Bearer ".concat(userDtoRefreshToken);

            ResponseEntity<LoginResponseDto> userDtoResponse = restTemplate.exchange(
                    createUrl("/refreshtoken"),
                    HttpMethod.PATCH,
                    requestWithBearerTokenForPatchMethod(userDtoBearerRefreshToken),
                    LoginResponseDto.class);

            String newAccessToken = userDtoResponse.getBody().accessToken();
            user = getUserByUserDto(userDto);

            assertEquals(newAccessToken, user.getAccessToken());
        } catch (RestClientException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void refreshAccessTokenShouldReturnedTokenBeValid() {
        try {
            Date expNow = Date.from(Instant.now());
            LocalDateTime expNowLocalDateTime = LocalDateTime.ofInstant(expNow.toInstant(), ZoneId.systemDefault());
            User user = createUser(userDto);

            String userDtoExpiredRefreshToken = authService.generateTokenWithCustomExpirationDate(userDto, expNow);
            updateRefreshTokenInDatabase(userDtoExpiredRefreshToken, user, expNowLocalDateTime);

            String userDtoExpiredBearerRefreshToken = "Bearer ".concat(userDtoExpiredRefreshToken);

            ResponseEntity<LoginResponseDto> userWithWrongRefreshTokenResponse = restTemplate.exchange(
                    createUrl("/refreshtoken"),
                    HttpMethod.PATCH,
                    requestWithBearerTokenForPatchMethod(userDtoExpiredBearerRefreshToken),
                    LoginResponseDto.class);

            assertEquals(HttpStatus.UNAUTHORIZED, userWithWrongRefreshTokenResponse.getStatusCode());
        } catch (RestClientException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    // ----------------------------------------------------------------------------------------------


    private HttpEntity<UserDto> requestWithBodyForPatchMethod(UserDto userDto) {
        var httpClient = HttpClientBuilder.create().build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        restTemplate.getRestTemplate().setRequestFactory(requestFactory);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(userDto, httpHeaders);
    }

    private HttpEntity<String> requestWithBearerTokenForPatchMethod(String bearerToken) {
        var httpClient = HttpClientBuilder.create().build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        restTemplate.getRestTemplate().setRequestFactory(requestFactory);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.TEXT_PLAIN);
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bearerToken);
        return new HttpEntity<>(bearerToken, httpHeaders);
    }

    @Transactional
    private void updateRefreshTokenInDatabase(String refreshToken, User userInDatabase) {
        userInDatabase.setRefreshToken(refreshToken);
        userInDatabase.setRefreshTokenExpirationDate(authService.getTokenExpirationDate(refreshToken));
        userRepository.save(userInDatabase);
    }

    @Transactional
    private void updateRefreshTokenInDatabase(String refreshToken, User userInDatabase, LocalDateTime dateTime) {
        userInDatabase.setRefreshToken(refreshToken);
        userInDatabase.setRefreshTokenExpirationDate(dateTime);
        userRepository.save(userInDatabase);
    }

    private UserDto createUserDto(String username, String password, String email, Set<Role> roles) {
        return new UserDto(null, username, password, email, false, roles, null, null, null, null);
    }

    @Transactional
    private User createUser(UserDto userDto) {
        User user = new User();
        user.setUsername(userDto.username());
        user.setPassword(authService.encryptPassword(userDto.password()));
        user.setEmail(userDto.email());
        user.setActive(true);
        user.setRoles(userDto.roles());
        userRepository.save(user);
        return user;
    }

    private String createUrl(String URI) {
        return "http://localhost:" + port + URI;
    }

    private User getUserByUserDto(UserDto userDto) {
        return userRepository.findByUsername(userDto.username());
    }

    private UserDto getUserDtoWithWrongCredentials(UserDto userDto) {
        createUser(userDto);

        return createUserDto(userDto.username(), "wrongpassword", userDto.email(), userDto.roles());

    }
}
