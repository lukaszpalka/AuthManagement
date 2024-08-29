package com.example.authmanagement.integration;

import com.example.authmanagement.config.PostgreSQLContainerConfig;
import com.example.authmanagement.auth.AuthService;
import com.example.authmanagement.auth.LoginResponseDto;
import com.example.authmanagement.enums.Role;
import com.example.authmanagement.user.User;
import com.example.authmanagement.user.UserDto;
import com.example.authmanagement.user.UserRepository;
import jakarta.transaction.Transactional;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
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

    @Autowired
    public UserControllerTest(final UserRepository userRepository, final AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @Test
    public void signUpTest() {
        UserDto userDto = createUserDto("logg", "pww", "mail@mial.com", Set.of(Role.USER, Role.ADMIN, Role.SUPER_ADMIN));

        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(
                    createUrl("/signup"),
                    userDto,
                    Void.class);

            User user = getUserByUserDto(userDto);

            assertEquals(response.getStatusCode(), HttpStatus.OK);
            assertNotNull(user);
        } catch (RestClientException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void signInTest() {
        UserDto userDto = createUserDto("log", "pw", "mail@mal.com", Set.of(Role.USER, Role.ADMIN, Role.SUPER_ADMIN));

        try {
            if (!userRepository.existsByUsername(userDto.username())) {
                createUser(userDto);
            }

            var httpClient = HttpClientBuilder.create().build();
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
            restTemplate.getRestTemplate().setRequestFactory(requestFactory);

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<UserDto> request = new HttpEntity<>(userDto, httpHeaders);

            ResponseEntity<LoginResponseDto> response = restTemplate.exchange(
                    createUrl("/signin"),
                    HttpMethod.PATCH,
                    request,
                    LoginResponseDto.class);

            String accessToken = response.getBody().accessToken();
            String refreshToken = response.getBody().refreshToken();

            User user = getUserByUserDto(userDto);

            assertEquals(response.getStatusCode(), HttpStatus.OK);
            assertNotNull(accessToken);
            assertNotNull(refreshToken);
            assertTrue(user.getTokenExpirationDate().isAfter(LocalDateTime.now()));
            assertTrue(user.getRefreshTokenExpirationDate().isAfter(LocalDateTime.now()));
        } catch (RestClientException e) {
            fail("Exception thrown: " + e.getMessage());
        }
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
