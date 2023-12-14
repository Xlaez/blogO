package com.dolph.blog.controllers.user;

import com.dolph.blog.dto.user.*;
import com.dolph.blog.dto.user.ResponseBody;
import com.dolph.blog.helpers.OtpGenerator;
import com.dolph.blog.helpers.PasswordValidator;
import com.dolph.blog.helpers.TimestampUtil;
import com.dolph.blog.interfaces.UserProjection;
import com.dolph.blog.models.User;
import com.dolph.blog.services.TokenService;
import com.dolph.blog.services.UserService;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    private final TokenService tokenService;

    @PostMapping
    @RequestMapping("/auth/register")
    public ResponseEntity<ResponseBody> createUser(@RequestBody NewUserRequest newUserRequest){
        try{
            ResponseBody response = new ResponseBody();

            Optional<User> user = userService.getUserByEmail(newUserRequest.getEmail());

            if(user.isPresent()){
                response.setStatus("failure");
                response.setMessage("user already exists, try singing in");
                return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
            }

            boolean isPasswordValid = PasswordValidator.isValidPassword(newUserRequest.getPassword());

            if(!isPasswordValid) {
                response.setStatus("failure");
                response.setMessage("password must be alphanumeric and at least 6 characters long");
                return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
            }

            String otp = OtpGenerator.newOtp();
            newUserRequest.setOtp(otp);
            newUserRequest.setOtpExpiry(TimestampUtil.getTimestampWithOffset(5));

            String id = userService.createUser(newUserRequest);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("userId", id);
            response.setStatus("success");
            response.setBody(responseBody);

            Map<String, Object> variables = new HashMap<>();

            variables.put("username", newUserRequest.getFullname());
            variables.put("otp", otp);

            System.out.println(otp);

            userService.sendEmail(newUserRequest.getEmail(), "Verify Email", variables );

            return new ResponseEntity<>(response,HttpStatus.CREATED);

        }catch (Exception e){
            log.error("Error creating user: {}", e.getMessage());
            ResponseBody response = new ResponseBody();
            response.setStatus("error");
            response.setMessage(e.getMessage());
            return new ResponseEntity<>(response,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    @RequestMapping("/auth/verify-otp")
    public ResponseEntity<ResponseBody> verifyEmail(@RequestBody VerifyOtpRequest request){
        try{
            ResponseBody response = new ResponseBody();

            Optional<User> user = userService.getUserByEmail(request.getEmail());

            if(user.isEmpty()){
                response.setStatus("failure");
                response.setMessage("user does not exists, try singing in");
                return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
            }else {
                User userDoc = user.get();
                if(userDoc.isEmailVerified()){
                    response.setStatus("failure");
                    response.setMessage("this account's email has been verified already");
                    return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
                }

                if(OtpGenerator.isOtpValid(userDoc.getOtpExpiry())){
                    if(!userDoc.getOtp().equals(request.getOtp())){
                        response.setStatus("failure");
                        response.setMessage("otp is not valid. try requesting for another");
                        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                    }

                    Query query =  new Query(Criteria.where("email").is(request.getEmail()));
                    Update update = new Update()
                            .set("isEmailVerified", true)
                            .set("otp", "")
                            .set("otpExpiry", "");

                    UpdateResult updatedUser = userService.updateUser(query, update);

                    if(updatedUser.getModifiedCount() == 0){
                        response.setStatus("failure");
                        response.setMessage("an error occurred, could not update user");
                        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                    }

                    Map<String, Map<String , Object>> tokens = tokenService.generateAuthTokens(userDoc.getId());

                    TokenRequest tokenRequest = new TokenRequest();
                    tokenRequest.setUserId(userDoc.getId());
                    tokenRequest.setToken(tokens.get("refreshToken").get("token").toString());
                    tokenRequest.setExpires(tokens.get("refreshToken").get("expires").toString());
                    tokenService.saveToken(tokenRequest);

                    Map<String, Object> returnData = new HashMap<>();

                    returnData.put("user", userService.mapUserToUserDTO(userDoc));
                    returnData.put("tokens", tokens);

                    response.setStatus("success");
                    response.setMessage("email verified successfully");
                    response.setBody(returnData);

                    return new ResponseEntity<>(response, HttpStatus.OK);

                }else{
                    response.setStatus("failure");
                    response.setMessage("otp has expired. try requesting for another");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            }

        }catch (Exception e){
            log.error("Error verifying otp: {}", e.getMessage());
            ResponseBody response = new ResponseBody();
            response.setStatus("error");
            response.setMessage(e.getMessage());
            return new ResponseEntity<>(response,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    @RequestMapping("/auth/login")
    public ResponseEntity<ResponseBody> login (@RequestBody LoginUserRequest request){
        try{
            ResponseBody response = new ResponseBody();

            Optional<User> user = userService.getUserByEmail(request.getEmail());

            if(user.isPresent()) {
                if(!PasswordValidator.isValidPassword(request.getPassword())){
                    response.setStatus("failure");
                    response.setMessage("password is invalid, it should be at least 6 characters and should be alphanumeric");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }

                if(!user.get().isEmailVerified()){
                    response.setStatus("failure");
                    response.setMessage("verify your email to continue");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }

                if(!userService.comparePassword(request.getPassword(), user.get().getPassword())){
                    response.setStatus("failure");
                    response.setMessage("login credentials incorrect");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }

                Map<String, Map<String , Object>> tokens = tokenService.generateAuthTokens(user.get().getId());

                TokenRequest tokenRequest = new TokenRequest();
                tokenRequest.setUserId(user.get().getId());
                tokenRequest.setToken(tokens.get("refreshToken").get("token").toString());
                tokenRequest.setExpires(tokens.get("refreshToken").get("expires").toString());
                tokenService.saveToken(tokenRequest);

                Map<String, Object> returnData = new HashMap<>();

                returnData.put("user", userService.mapUserToUserDTO(user.get()));
                returnData.put("tokens", tokens);

                response.setStatus("success");
                response.setMessage("");
                response.setBody(returnData);

                return new ResponseEntity<>(response, HttpStatus.OK);
            }

            response.setStatus("failure");
            response.setMessage("login credentials incorrect");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

        }catch (Exception e){
            log.error("Error signing user in: {}", e.getMessage());
            ResponseBody response = new ResponseBody();
            response.setStatus("error");
            response.setMessage(e.getMessage());
            return new ResponseEntity<>(response,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    @RequestMapping("/auth/logout/{token}")
    public ResponseEntity<ResponseBody> logout (@PathVariable String token){
       try{
           ResponseBody response = new ResponseBody();
           if(tokenService.deleteToken(token).getDeletedCount() == 0){
               response.setStatus("failure");
               response.setMessage("error signing user out");
               return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
           }

           response.setStatus("success");
           return new ResponseEntity<>(response, HttpStatus.OK);
       }catch (Exception e){
           log.error("Error signing user out: {}", e.getMessage());
           ResponseBody response = new ResponseBody();
           response.setStatus("error");
           response.setMessage(e.getMessage());
           return new ResponseEntity<>(response,HttpStatus.INTERNAL_SERVER_ERROR);
       }
    }

    // TODO: refresh tokens

    @GetMapping
    @RequestMapping("/users/1/{userId}")
    public ResponseEntity<ResponseBody> getUserByID (@PathVariable String userId,
                                                     @AuthenticationPrincipal String id){
     try{
         ResponseBody response = new ResponseBody();
         UserProjection user = userService.getUserByIdProjection(userId);

         if(user.getEmail().isEmpty()){
             response.setStatus("failure");
             response.setMessage("cannot fetch user data");
             return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
         }

         Map<String, Object> userDoc = new HashMap<>();

         userDoc.put("user", user);

         response.setStatus("success");
         response.setBody(userDoc);
         return new ResponseEntity<>(response, HttpStatus.OK);

     }catch (Exception e){
         log.error("Error getting user data: {}", e.getMessage());
         ResponseBody response = new ResponseBody();
         response.setStatus("error");
         response.setMessage(e.getMessage());
         return new ResponseEntity<>(response,HttpStatus.INTERNAL_SERVER_ERROR);
     }
    }
}
