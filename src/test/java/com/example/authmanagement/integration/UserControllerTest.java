package com.example.authmanagement.integration;

import com.example.authmanagement.PostgreSQLContainerConfig;
import com.example.authmanagement.auth.AuthService;
import com.example.authmanagement.auth.LoginResponseDto;
import com.example.authmanagement.enums.Role;
import com.example.authmanagement.user.User;
import com.example.authmanagement.user.UserController;
import com.example.authmanagement.user.UserDto;
import com.example.authmanagement.user.UserRepository;
import jakarta.transaction.Transactional;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest extends PostgreSQLContainerConfig {

    @LocalServerPort
    private int port;

//    @Autowired
//    private final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(HttpClientBuilder.create().build()));

//    @Autowired
//    private final TestRestTemplate restTemplate = new TestRestTemplate();

//    @Autowired
//    private final RestTemplate restTemplate;

    private final UserRepository userRepository;
    private final AuthService authService;

    @Autowired
    public UserControllerTest(final UserRepository userRepository, final AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @Transactional
    @Test
    public void signUpTest() {
        UserDto userDto = new UserDto(
                null,
                "login",
                "pw",
                "mail@mail.com",
                false,
                Set.of(Role.USER, Role.ADMIN, Role.SUPER_ADMIN),
                null,
                null,
                null,
                null);

        try {
//            ResponseEntity<Void> response = restTemplate.postForEntity(
//                    "http://localhost:" + port + "/signup",
//                    userDto,
//                    Void.class);

            User user = userRepository.findByUsername(userDto.username());
//            assertEquals(response.getStatusCode(), HttpStatus.OK);
            assertNotNull(user);
        } catch (RestClientException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Transactional
    @Test
    public void signInTest() {
//        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        TestRestTemplate restTemplate = new TestRestTemplate();

        UserDto userDto = new UserDto(
                null,
                "login",
                "pw",
                "mail@mail.com",
                true,
                Set.of(Role.USER, Role.ADMIN, Role.SUPER_ADMIN),
                null,
                null,
                null,
                null);

        try {
            if (!userRepository.existsByUsername(userDto.username())) {
                User testUser = new User();
                testUser.setUsername(userDto.username());
                testUser.setPassword(authService.encryptPassword(userDto.password()));
                testUser.setEmail(userDto.email());
                testUser.setActive(true);
                testUser.setRoles(userDto.roles());
                userRepository.save(testUser);
            }

//            ResponseEntity<LoginResponseDto> loginResponse = restTemplate.postForEntity(
//                    "http://localhost:" + port + "/signup",
//                    userDto,
//                    LoginResponseDto.class);


            String url = "http://localhost:" + port + "/signin";


        var httpClient = HttpClientBuilder.create().build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        restTemplate.getRestTemplate().setRequestFactory(requestFactory);

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<UserDto> request = new HttpEntity<>(userDto, httpHeaders);

            ResponseEntity<LoginResponseDto> loginResponseDtoResponseEntity = restTemplate.patchForObject(
                    url,
                    request,
                    ResponseEntity.class);

//            ResponseEntity<LoginResponseDto> response = restTemplate.exchange(
//                    url,
//                    HttpMethod.PATCH,
//                    request,
//                    LoginResponseDto.class);
//
//            String accessToken = response.getBody().accessToken();
//            String refreshToken = response.getBody().refreshToken();

//            User user = userRepository.findByUsername(userDto.username());

//            assertEquals(loginResponse.getStatusCode(), HttpStatus.OK);
//            assertNotNull(accessToken);
//            assertNotNull(refreshToken);
//            assertTrue(user.getTokenExpirationDate().isAfter(LocalDateTime.now()));
//            assertTrue(user.getRefreshTokenExpirationDate().isAfter(LocalDateTime.now()));
        } catch (RestClientException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
}
