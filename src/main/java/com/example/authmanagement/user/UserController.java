package com.example.authmanagement.user;

import com.example.authmanagement.auth.LoginResponseDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(final UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user/all")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return new ResponseEntity<>(userService.getUserDtos(), HttpStatus.OK);
    }

    @PostMapping("/signup")
    public ResponseEntity signUp(@RequestBody UserDto userDto) {
        userService.signUp(userDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/signin")
    public ResponseEntity<LoginResponseDto> signIn(@RequestBody UserDto userDto) {
        return new ResponseEntity<>(userService.signIn(userDto), HttpStatus.OK);
    }

    @PatchMapping("/refreshtoken")
    public ResponseEntity<LoginResponseDto> refreshAccessToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String bearerRefreshToken) {
        return new ResponseEntity<>(userService.refreshAccessToken(bearerRefreshToken), HttpStatus.OK);
    }

    @PatchMapping("/user/activate/{id}")
    public ResponseEntity activateAccount(@PathVariable("id") Long id) {
        userService.activateAccount(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/user/roles")
    public ResponseEntity updateRoles(@RequestBody UserDto userDto) {
        userService.updateRoles(userDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
