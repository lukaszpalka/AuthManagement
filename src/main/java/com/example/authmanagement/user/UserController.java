package com.example.authmanagement.user;

import com.example.authmanagement.action.ActionService;
import com.example.authmanagement.auth.AuthService;
import com.example.authmanagement.enums.Operation;
import com.example.authmanagement.enums.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final ActionService actionService;

    public UserController(final UserService userService, final ActionService actionService) {
        this.userService = userService;
        this.actionService = actionService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return new ResponseEntity<>(userService.getUserDtos(), HttpStatus.OK);
    }

    @PostMapping("/signup")
    public ResponseEntity signUp(@RequestBody UserDto userDto) {
        userService.signUp(userDto);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PatchMapping("/signin")
    public ResponseEntity<String> signIn(@RequestBody UserDto userDto) {
        String token = userService.signIn(userDto);
        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity deleteUser(@PathVariable("id") Long id) {
        userService.removeUser(id);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PatchMapping("/action")
    public ResponseEntity doAction(@RequestParam("id") Long id, @RequestParam("operation") Operation operation, @RequestParam("resource") Resource resource) {
        //TODO: dopasować pod weryfikację JWT i roli
        actionService.doAction(id, operation, resource);
        return new ResponseEntity(HttpStatus.OK);
    }

}
