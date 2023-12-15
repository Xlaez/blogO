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
import com.dolph.blog.utils.ErrorHandler;
import com.dolph.blog.utils.FileUploader;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class UserController {

    private final FileUploader fileUploader;
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
            ResponseBody response =  ErrorHandler.catchHandler(e, "Error creating user: {} ");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
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
            ResponseBody response =  ErrorHandler.catchHandler(e, "Error verifying otp: {} ");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
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
            ResponseBody response =  ErrorHandler.catchHandler(e, "Error signing user in: ");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
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
           ResponseBody response =  ErrorHandler.catchHandler(e, "Error signing user out: {} ");
           return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
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

         if(user == null){
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
         ResponseBody response =  ErrorHandler.catchHandler(e, "Error getting user data: {} ");
         return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
     }
    }

    @PutMapping
    @RequestMapping("/users/update")
    public ResponseEntity<ResponseBody> updateUser (@AuthenticationPrincipal String id,
                                                    @RequestBody UpdateUserRequest request){
        try{
            ResponseBody response = new ResponseBody();
            UserProjection user = userService.getUserByIdProjection(id);

            if(user == null){
                response.setStatus("failure");
                response.setMessage("user not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            Query query =  new Query(Criteria.where("_id").is(user.getId()));
            Update update = new Update();

            if(!request.getBio().isEmpty()){
                update.set("bio", request.getBio());
            }

            if(!request.getTwitter().isEmpty()){
                update.set("twitter", request.getTwitter());
            }

            if(userService.updateUser(query, update).getModifiedCount() == 0){
                response.setStatus("failure");
                response.setMessage("cannot update user data");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            response.setStatus("success");
            response.setMessage("user data updated");
            return new ResponseEntity<>(response, HttpStatus.OK);

        }catch (Exception e){
            ResponseBody response =  ErrorHandler.catchHandler(e, "Error updating user data: {} ");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping
    @RequestMapping("/users/1")
    public ResponseEntity<ResponseBody> deleteUser(@AuthenticationPrincipal String id){
      try{
          ResponseBody response = new ResponseBody();
          UserProjection user = userService.getUserByIdProjection(id);

          if(user == null){
              response.setStatus("failure");
              response.setMessage("user not found");
              return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
          }

          if(userService.deleteUserById(user.getId()) == 0){
              response.setStatus("failure");
              response.setMessage("failed to delete user data");
              return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
          }

          response.setStatus("success");
          response.setMessage("user deleted");
          return new ResponseEntity<>(response, HttpStatus.OK);

      }catch(Exception e){
          ResponseBody response =  ErrorHandler.catchHandler(e, "Error deleting user : {} ");
          return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }

    @GetMapping
    @RequestMapping("/users")
    public ResponseEntity<ResponseBody> getUsers(@RequestParam() int page,
                                                 @RequestParam() int limit){
        try{
            ResponseBody response = new ResponseBody();

            Page<User> users = userService.fetchUsers(page, limit);

            List<User> userList = users.getContent();

            if(userList.isEmpty()){
                response.setStatus("failure");
                response.setMessage("there are no users yet");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            List<UserDataResponse> userDocuments = new ArrayList<>();
            for (User user: userList) {
                userDocuments.add(userService.mapUserToUserDTO(user));
            }

            long totalUsers = users.getTotalElements();
            int totalPages =users.getTotalPages();
            boolean nextPage = users.hasNext();

            Map<String, Object> returnDoc = new HashMap<>();

            returnDoc.put("totalDocs", totalUsers);
            returnDoc.put("totalPages", totalPages);
            returnDoc.put("hasNextPage", nextPage);
            returnDoc.put("docs", userDocuments);

            response.setStatus("success");
            response.setMessage("fetched " + users.getNumberOfElements() + " successfully");
            response.setBody(returnDoc);

            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch(Exception e){
            ResponseBody response =  ErrorHandler.catchHandler(e, "Error getting users: {} ");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping
    @RequestMapping("/users/update/pic")
    public ResponseEntity<ResponseBody> updatePic(@AuthenticationPrincipal String id,
                                                  @RequestParam("upload")MultipartFile upload){
        try{
            ResponseBody response = new ResponseBody();
            String fileUrl = fileUploader.uploadFile(upload);

            Query query =  new Query(Criteria.where("_id").is(id));
            Update update = new Update();

            update.set("pics", fileUrl);

            if(userService.updateUser(query, update).getModifiedCount() == 0){
                response.setStatus("failure");
                response.setMessage("cannot update user data");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            response.setStatus("success");
            response.setMessage("user pic updated");
            return new ResponseEntity<>(response, HttpStatus.OK);

        }catch (Exception e){
          ResponseBody response =  ErrorHandler.catchHandler(e, "Error updating user pics: {} ");
          return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

//TODO: refresh tokens, reset password, change email