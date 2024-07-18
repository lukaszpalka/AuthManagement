package com.example.authmanagement.user;

import com.example.authmanagement.action.ActionService;
import com.example.authmanagement.enums.Operation;
import com.example.authmanagement.enums.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/signin")
    public ResponseEntity<String> signIn(@RequestBody UserDto userDto) {
        return new ResponseEntity<>(userService.signIn(userDto), HttpStatus.OK);
    }

    @PatchMapping("/refreshtoken")
    public ResponseEntity<String> refreshAccessToken(@RequestHeader(name = "Authorization") String refreshBearerToken) {
        return new ResponseEntity<>(userService.refreshAccessToken(refreshBearerToken), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity deleteUser(@PathVariable("id") Long id) {
        userService.removeUser(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/action")
    public ResponseEntity doAction(@RequestParam("id") Long id, @RequestParam("operation") Operation operation, @RequestParam("resource") Resource resource) {
        //TODO: dopasować pod weryfikację JWT i roli
        actionService.doAction(id, operation, resource);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
