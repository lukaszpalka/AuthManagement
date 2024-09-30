package com.example.authmanagement.integration;

import com.example.authmanagement.auth.AuthService;
import com.example.authmanagement.auth.JWTUtil;
import com.example.authmanagement.config.PostgreSQLContainerConfig;
import com.example.authmanagement.enums.Role;
import com.example.authmanagement.user.User;
import com.example.authmanagement.user.UserDto;
import com.example.authmanagement.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ActionControllerTest extends PostgreSQLContainerConfig {

    @LocalServerPort
    private int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();
    private final UserRepository userRepository;
    private final AuthService authService;
    private final JWTUtil jwtUtil;

    @Autowired
    public ActionControllerTest(final UserRepository userRepository, final AuthService authService, final JWTUtil jwtUtil) {
        this.userRepository = userRepository;
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    private UserDto userDto;
    private UserDto adminDto;
    private UserDto superAdminDto;

    @Transactional
    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
        userDto = createUserDto("user", "user", "user@nomail.com", Set.of(Role.USER));
        adminDto = createUserDto("admin", "admin", "admin@nomail.com", Set.of(Role.USER, Role.ADMIN));
        superAdminDto = createUserDto("superadmin", "superadmin", "superadmin@nomail.com", Set.of(Role.USER, Role.ADMIN, Role.SUPER_ADMIN));
    }

    @Test
    public void getProductShouldReturnOkForUser() {
        try {
            User user = createAuthenticatedUser(userDto);
            HttpEntity<String> request = createRequestWithAuthorizationHeader(user);

            ResponseEntity<String> userResponse = restTemplate.exchange(
                    createUrl("/product/get"),
                    HttpMethod.GET,
                    request,
                    String.class);

            assertEquals(HttpStatus.OK, userResponse.getStatusCode());
        } catch (RestClientException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void addProductShouldReturnOkForAdmin() {
        try {
            User admin = createAuthenticatedUser(adminDto);
            HttpEntity<String> request = createRequestWithAuthorizationHeader(admin);

            ResponseEntity<String> adminResponse = restTemplate.exchange(
                    createUrl("/product/add"),
                    HttpMethod.POST,
                    request,
                    String.class);

            assertEquals(HttpStatus.OK, adminResponse.getStatusCode());
        } catch (RestClientException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void addProductShouldReturnUnauthorizedForUser() {
        try {
            User user = createAuthenticatedUser(userDto);
            HttpEntity<String> request = createRequestWithAuthorizationHeader(user);

            ResponseEntity<String> userResponse = restTemplate.exchange(
                    createUrl("/product/add"),
                    HttpMethod.POST,
                    request,
                    String.class);

            assertEquals(HttpStatus.UNAUTHORIZED, userResponse.getStatusCode());
        } catch (RestClientException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void deleteUserShouldReturnNoContentForSuperAdmin() {
        try {
            User user = createAuthenticatedUser(userDto);
            User superAdmin = createAuthenticatedUser(superAdminDto);
            HttpEntity<String> request = createRequestWithAuthorizationHeader(superAdmin);

            ResponseEntity<String> superAdminResponse = restTemplate.exchange(
                    createUrl("/user/delete/" + user.getId()),
                    HttpMethod.DELETE,
                    request,
                    String.class);

            assertEquals(HttpStatus.NO_CONTENT, superAdminResponse.getStatusCode());
        } catch (RestClientException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void deleteUserShouldReturnNotFoundWhenUserNotExists() {
        try {
            User user = createAuthenticatedUser(userDto);
            Long id = user.getId();
            userRepository.delete(user);
            User superAdmin = createAuthenticatedUser(superAdminDto);
            HttpEntity<String> request = createRequestWithAuthorizationHeader(superAdmin);

            ResponseEntity<String> superAdminResponse = restTemplate.exchange(
                    createUrl("/user/delete/" + id),
                    HttpMethod.DELETE,
                    request,
                    String.class);

            assertEquals(HttpStatus.NOT_FOUND, superAdminResponse.getStatusCode());
        } catch (RestClientException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

//    ------------------------------------------------------------------------

    private HttpEntity<String> createRequestWithAuthorizationHeader(User user) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + user.getAccessToken());
        return new HttpEntity<>(httpHeaders);
    }

    private UserDto createUserDto(String username, String password, String email, Set<Role> roles) {
        return new UserDto(null, username, password, email, false, roles, null, null, null, null);
    }

    @Transactional
    private User createAuthenticatedUser(UserDto userDto) {
        User user = new User();

        user.setUsername(userDto.username());
        user.setPassword(authService.encryptPassword(userDto.password()));
        user.setEmail(userDto.email());
        user.setActive(true);
        user.setRoles(userDto.roles());
        userRepository.save(user);

        userDto = new UserDto(user.getId(), user.getUsername(), user.getPassword(), user.getEmail(), false, user.getRoles(), user.getAccessToken(), user.getAccessTokenExpirationDate(), user.getRefreshToken(), user.getRefreshTokenExpirationDate());
        String accessToken = authService.generateAccessToken(userDto);
        String refreshToken = authService.generateRefreshToken(userDto);
        user.setAccessToken(accessToken);
        user.setRefreshToken(refreshToken);
        user.setAccessTokenExpirationDate(authService.getTokenExpirationDate(accessToken));
        user.setRefreshTokenExpirationDate(authService.getTokenExpirationDate(accessToken));
        userRepository.save(user);

        return user;
    }

    private String createUrl(String URI) {
        return "http://localhost:" + port + "/action" + URI;
    }
}
