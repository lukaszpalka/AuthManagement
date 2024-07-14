package com.example.authmanagement.action;

import com.example.authmanagement.enums.Operation;
import com.example.authmanagement.enums.Resource;
import com.example.authmanagement.enums.Role;
import com.example.authmanagement.exceptions.NoAccessException;
import com.example.authmanagement.user.UserDto;
import com.example.authmanagement.user.UserService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ActionService {

    public void doAction(Long id, Operation operation, Resource resource) {
            System.out.println("User with id " + id + " did action: " + operation + " " + resource + ".");
    }

}
