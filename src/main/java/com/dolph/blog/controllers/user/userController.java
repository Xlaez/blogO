package com.dolph.blog.controllers.user;

import com.dolph.blog.dto.user.NewUserRequest;
import com.dolph.blog.dto.user.ResponseBody;
import com.dolph.blog.helpers.PasswordValidator;
import com.dolph.blog.services.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class userController {
    private final UserService userService;

    @PostMapping
    @RequestMapping("/auth/register")
    public ResponseEntity<ResponseBody> createUser(@RequestBody NewUserRequest newUserRequest){
        try{
            String id = userService.createUser(newUserRequest);

            boolean isPasswordValid = PasswordValidator.isValidPassword(newUserRequest.getPassword());

            ResponseBody response = new ResponseBody();

            if(!isPasswordValid) {
                response.setStatus("failure");
                response.setMessage("password must be alphanumeric and at least 6 characters long");
                return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
            }

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("userId", id);
            response.setStatus("success");
            response.setBody(responseBody);
            return new ResponseEntity<>(response,HttpStatus.CREATED);

        }catch (Exception e){
            log.error("Error creating user: {}", e.getMessage());
            ResponseBody response = new ResponseBody();
            response.setStatus("error");
            response.setMessage(e.getMessage());
            return new ResponseEntity<>(response,HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }
}
