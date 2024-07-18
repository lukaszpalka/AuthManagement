package com.example.authmanagement.action;

import com.example.authmanagement.enums.Operation;
import com.example.authmanagement.enums.Resource;
import org.springframework.stereotype.Service;

@Service
public class ActionService {

    public void doAction(Long id, Operation operation, Resource resource) {
        System.out.println("User with id " + id + " did action: " + operation + " " + resource + ".");
    }

}
