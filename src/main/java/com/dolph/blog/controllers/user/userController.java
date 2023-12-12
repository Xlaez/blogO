package com.dolph.blog.controllers.user;

import com.dolph.blog.dto.user.NewUserRequest;
import com.dolph.blog.services.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class userController {
    private final UserService userService;

    @PostMapping
    @RequestMapping("/auth/register")
    public ResponseEntity<String> createUser(@RequestBody NewUserRequest newUserRequest){
        try{
            String id = userService.createUser(newUserRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully: " + id);
        }catch (Exception e){
            log.error("Error creating user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}
