package com.example.authmanagement.action;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/action")
public class ActionController {

    private final ActionService actionService;

    public ActionController(final ActionService actionService) {
        this.actionService = actionService;
    }

    @GetMapping("/product/get")
    public ResponseEntity<String> getProduct(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        return new ResponseEntity<>(actionService.getProduct(token), HttpStatus.OK);
    }

    @PostMapping("/product/add")
    public ResponseEntity addProduct(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        actionService.addProduct(token);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/product/modify")
    public ResponseEntity modifyProduct(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        actionService.modifyProduct(token);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/product/delete")
    public ResponseEntity deleteProduct(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        actionService.deleteProduct(token);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/category/get")
    public ResponseEntity<String> getCategory(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        return new ResponseEntity<>(actionService.getCategory(token), HttpStatus.OK);
    }

    @PostMapping("/category/add")
    public ResponseEntity addCategory(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        actionService.addCategory(token);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/category/modify")
    public ResponseEntity modifyCategory(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        actionService.modifyCategory(token);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/category/delete")
    public ResponseEntity deleteCategory(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        actionService.deleteCategory(token);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/user/delete/{id}")
    public ResponseEntity deleteUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String token, @PathVariable("id") Long id) {
        SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        actionService.deleteUser(token, id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


}
