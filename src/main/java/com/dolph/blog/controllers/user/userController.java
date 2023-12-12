package com.dolph.blog.controllers.user;

import com.dolph.blog.dto.user.NewUserRequest;
import com.dolph.blog.services.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/user")
@RequiredArgsConstructor
public class userController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping("/register")
    public void createUser(@RequestBody NewUserRequest newUserRequest){
        userService.createUser(newUserRequest);
    }
}
