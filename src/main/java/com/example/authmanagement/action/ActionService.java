package com.example.authmanagement.action;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.authmanagement.auth.AuthService;
import com.example.authmanagement.enums.Operation;
import com.example.authmanagement.enums.Resource;
import com.example.authmanagement.exceptions.NoAccessException;
import com.example.authmanagement.exceptions.UserNotFoundException;
import com.example.authmanagement.user.User;
import com.example.authmanagement.user.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class ActionService {

    private final UserRepository userRepository;
    private final AuthService authService;

    public ActionService(final UserRepository userRepository, final AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    public String getProduct(String token) {
        if (hasAccessTo(getIdFromToken(token), Operation.GET, Resource.PRODUCT)) {
            System.out.println("Product returned");
            return "Product";
        } else throw new NoAccessException("You don't have permission to do that");
    }

    public void addProduct(String token) {
        if (hasAccessTo(getIdFromToken(token), Operation.ADD, Resource.PRODUCT)) {
            System.out.println("Product added");
        } else throw new NoAccessException("You don't have permission to do that");
    }

    public void modifyProduct(String token) {
        if (hasAccessTo(getIdFromToken(token), Operation.MODIFY, Resource.PRODUCT)) {
            System.out.println("Product modified");
        } else throw new NoAccessException("You don't have permission to do that");
    }

    public void deleteProduct(String token) {
        if (hasAccessTo(getIdFromToken(token), Operation.DELETE, Resource.PRODUCT)) {
            System.out.println("Product deleted");
        } else throw new NoAccessException("You don't have permission to do that");
    }

    public String getCategory(String token) {
        if (hasAccessTo(getIdFromToken(token), Operation.GET, Resource.CATEGORY)) {
            System.out.println("Category returned");
            return "Category";
        } else throw new NoAccessException("You don't have permission to do that");
    }

    public void addCategory(String token) {
        if (hasAccessTo(getIdFromToken(token), Operation.ADD, Resource.CATEGORY)) {
            System.out.println("Category added");
        } else throw new NoAccessException("You don't have permission to do that");
    }

    public void modifyCategory(String token) {
        if (hasAccessTo(getIdFromToken(token), Operation.MODIFY, Resource.CATEGORY)) {
            System.out.println("Category modified");
        } else throw new NoAccessException("You don't have permission to do that");
    }

    public void deleteCategory(String token) {
        if (hasAccessTo(getIdFromToken(token), Operation.DELETE, Resource.CATEGORY)) {
            System.out.println("Category deleted");
        } else throw new NoAccessException("You don't have permission to do that");
    }

    public void deleteUser(String token, Long id) {
        if (!hasAccessTo(getIdFromToken(token), Operation.DELETE, Resource.USER)) {
            throw new NoAccessException("You don't have permission to do that");
        }
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User with id=" + id + " doesn't exist"));
        userRepository.delete(user);
    }

    private Long getIdFromToken(String token) {
        if (token.startsWith("Bearer ")) {
            DecodedJWT decodedJWT = authService.verifyToken(token.substring(7));
            return decodedJWT.getClaim("id").asLong();
        } else throw new UserNotFoundException("Bearer token not provided");
    }

    private boolean hasAccessTo(Long id, Operation operation, Resource resource) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found."))
                .getRoles()
                .stream()
                .anyMatch(role -> role.hasAccessTo(operation, resource));
    }
}
