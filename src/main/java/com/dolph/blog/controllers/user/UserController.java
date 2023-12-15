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
import com.dolph.blog.utils.ApiResponse;
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
        ApiResponse response = new ApiResponse();

        try{
            Optional<User> user = userService.getUserByEmail(newUserRequest.getEmail());

            if(user.isPresent()){
               ResponseBody r = response.failureResponse("user already exists, try singing in", null);
                return new ResponseEntity<>(r,HttpStatus.BAD_REQUEST);
            }

            boolean isPasswordValid = PasswordValidator.isValidPassword(newUserRequest.getPassword());

            if(!isPasswordValid) {
                ResponseBody r = response.failureResponse("password must be alphanumeric and at least 6 characters long", null);
                return new ResponseEntity<>(r,HttpStatus.BAD_REQUEST);
            }

            String otp = OtpGenerator.newOtp();
            newUserRequest.setOtp(otp);
            newUserRequest.setOtpExpiry(TimestampUtil.getTimestampWithOffset(5));

            String id = userService.createUser(newUserRequest);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("userId", id);

            ResponseBody r = response.successResponse("", responseBody);

            Map<String, Object> variables = new HashMap<>();

            variables.put("username", newUserRequest.getFullname());
            variables.put("otp", otp);

            System.out.println(otp);

            userService.sendEmail(newUserRequest.getEmail(), "Verify Email", variables );

            return new ResponseEntity<>(r,HttpStatus.CREATED);

        }catch (Exception e){
            ResponseBody r =response.catchHandler(e, "Error creating user: {} ");
            return new ResponseEntity<>(r, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    @RequestMapping("/auth/verify-otp")
    public ResponseEntity<ResponseBody> verifyEmail(@RequestBody VerifyOtpRequest request){
        ApiResponse response = new ApiResponse();

        try{
            Optional<User> user = userService.getUserByEmail(request.getEmail());

            if(user.isEmpty()){
                ResponseBody r = response.failureResponse("user does not exists, try singing in", null);
                return new ResponseEntity<>(r,HttpStatus.BAD_REQUEST);
            }else {
                User userDoc = user.get();
                if(userDoc.isEmailVerified()){
                    ResponseBody r =response.failureResponse("this account's email has been verified already", null);
                    return new ResponseEntity<>(r,HttpStatus.BAD_REQUEST);
                }

                if(OtpGenerator.isOtpValid(userDoc.getOtpExpiry())){
                    if(!userDoc.getOtp().equals(request.getOtp())){
                        ResponseBody r =response.failureResponse("otp is not valid. try requesting for another", null);
                        return new ResponseEntity<>(r, HttpStatus.BAD_REQUEST);
                    }

                    Query query =  new Query(Criteria.where("email").is(request.getEmail()));
                    Update update = new Update()
                            .set("isEmailVerified", true)
                            .set("otp", "")
                            .set("otpExpiry", "");

                    UpdateResult updatedUser = userService.updateUser(query, update);

                    if(updatedUser.getModifiedCount() == 0){
                        ResponseBody r =response.failureResponse("an error occurred, could not update user", null);
                        return new ResponseEntity<>(r, HttpStatus.BAD_REQUEST);
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

                    ResponseBody r = response.successResponse("email verified successfully", returnData);
                    return new ResponseEntity<>(r, HttpStatus.OK);

                }else{
                    ResponseBody r =response.failureResponse("otp has expired. try requesting for another", null);
                    return new ResponseEntity<>(r, HttpStatus.BAD_REQUEST);
                }
            }

        }catch (Exception e){
            ResponseBody r = response.catchHandler(e, "Error verifying otp: {} ");
            return new ResponseEntity<>(r, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    @RequestMapping("/auth/login")
    public ResponseEntity<ResponseBody> login (@RequestBody LoginUserRequest request){
        ApiResponse response = new ApiResponse();

        try{
            Optional<User> user = userService.getUserByEmail(request.getEmail());

            if(user.isPresent()) {
                if(!PasswordValidator.isValidPassword(request.getPassword())){
                    ResponseBody r = response.failureResponse(
                            "password is invalid, it should be at least 6 characters and should be alphanumeric", null);
                    return new ResponseEntity<>(r, HttpStatus.BAD_REQUEST);
                }

                if(!user.get().isEmailVerified()){
                    ResponseBody r = response.failureResponse("verify your email to continue", null);
                    return new ResponseEntity<>(r, HttpStatus.BAD_REQUEST);
                }

                if(!userService.comparePassword(request.getPassword(), user.get().getPassword())){
                    ResponseBody r =response.failureResponse("login credentials incorrect", null);
                    return new ResponseEntity<>(r, HttpStatus.BAD_REQUEST);
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

                ResponseBody r = response.successResponse("", returnData);
                return new ResponseEntity<>(r, HttpStatus.OK);
            }

            ResponseBody r =  response.failureResponse("login credentials incorrect", null);
            return new ResponseEntity<>(r, HttpStatus.BAD_REQUEST);

        }catch (Exception e){
            ResponseBody r =  response.catchHandler(e, "Error signing user in: ");
            return new ResponseEntity<>(r, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    @RequestMapping("/auth/logout/{token}")
    public ResponseEntity<ResponseBody> logout (@PathVariable String token){
        ApiResponse response = new ApiResponse();

       try{
           if(tokenService.deleteToken(token).getDeletedCount() == 0){
               ResponseBody r =response.failureResponse("error signing user out", null);
               return new ResponseEntity<>(r, HttpStatus.INTERNAL_SERVER_ERROR);
           }

           ResponseBody r =response.successResponse("",null);
           return new ResponseEntity<>(r, HttpStatus.OK);
       }catch (Exception e){
           ResponseBody r = response.catchHandler(e, "Error signing user out: {} ");
           return new ResponseEntity<>(r, HttpStatus.INTERNAL_SERVER_ERROR);
       }
    }

    @GetMapping
    @RequestMapping("/users/1/{userId}")
    public ResponseEntity<ResponseBody> getUserByID (@PathVariable String userId,
                                                     @AuthenticationPrincipal String id){
        ApiResponse response = new ApiResponse();

     try{
         UserProjection user = userService.getUserByIdProjection(userId);

         if(user == null){
             ResponseBody r = response.failureResponse("cannot fetch user data", null);
             return new ResponseEntity<>(r, HttpStatus.NOT_FOUND);
         }

         Map<String, Object> userDoc = new HashMap<>();

         userDoc.put("user", user);

         ResponseBody r =  response.successResponse("", userDoc);
         return new ResponseEntity<>(r, HttpStatus.OK);

     }catch (Exception e){
         ResponseBody r = response.catchHandler(e, "Error getting user data: {} ");
         return new ResponseEntity<>(r, HttpStatus.INTERNAL_SERVER_ERROR);
     }
    }

    @PutMapping
    @RequestMapping("/users/update")
    public ResponseEntity<ResponseBody> updateUser (@AuthenticationPrincipal String id,
                                                    @RequestBody UpdateUserRequest request){
        ApiResponse response = new ApiResponse();

        try{
            UserProjection user = userService.getUserByIdProjection(id);

            if(user == null){
                ResponseBody r = response.failureResponse("user not found", null);
                return new ResponseEntity<>(r, HttpStatus.NOT_FOUND);
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
                ResponseBody r =response.failureResponse("cannot update user data", null);
                return new ResponseEntity<>(r, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            ResponseBody r =response.successResponse("user data updated",null);
            return new ResponseEntity<>(r, HttpStatus.OK);

        }catch (Exception e){
            ResponseBody r =response.catchHandler(e, "Error updating user data: {} ");
            return new ResponseEntity<>(r, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping
    @RequestMapping("/users/1")
    public ResponseEntity<ResponseBody> deleteUser(@AuthenticationPrincipal String id){
        ApiResponse response = new ApiResponse();

      try{
          UserProjection user = userService.getUserByIdProjection(id);

          if(user == null){
              ResponseBody r = response.failureResponse("user not found", null);
              return new ResponseEntity<>(r, HttpStatus.NOT_FOUND);
          }

          if(userService.deleteUserById(user.getId()) == 0){
              ResponseBody r =response.failureResponse("failed to delete user data", null);
              return new ResponseEntity<>(r, HttpStatus.INTERNAL_SERVER_ERROR);
          }

          ResponseBody r = response.successResponse("user deleted", null);
          return new ResponseEntity<>(r, HttpStatus.OK);

      }catch(Exception e){
          ResponseBody r =response.catchHandler(e, "Error deleting user : {} ");
          return new ResponseEntity<>(r, HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }

    @GetMapping
    @RequestMapping("/users")
    public ResponseEntity<ResponseBody> getUsers(@RequestParam() int page,
                                                 @RequestParam() int limit){
        ApiResponse response = new ApiResponse();

        try{
            Page<User> users = userService.fetchUsers(page, limit);

            List<User> userList = users.getContent();

            if(userList.isEmpty()){
                ResponseBody r =response.failureResponse("there are no users yet", null);
                return new ResponseEntity<>(r, HttpStatus.NOT_FOUND);
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

            ResponseBody r =response.successResponse("fetched " + users.getNumberOfElements() + " successfully", returnDoc);

            return new ResponseEntity<>(r, HttpStatus.OK);
        }catch(Exception e){
            ResponseBody r =response.catchHandler(e, "Error getting users: {} ");
            return new ResponseEntity<>(r, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping
    @RequestMapping("/users/update/pic")
    public ResponseEntity<ResponseBody> updatePic(@AuthenticationPrincipal String id,
                                                  @RequestParam("upload")MultipartFile upload){
        ApiResponse response = new ApiResponse();

        try{

            String fileUrl = fileUploader.uploadFile(upload);

            Query query =  new Query(Criteria.where("_id").is(id));
            Update update = new Update();

            update.set("pics", fileUrl);

            if(userService.updateUser(query, update).getModifiedCount() == 0){
                ResponseBody r = response.failureResponse("cannot update user data", null);
                return new ResponseEntity<>(r, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            ResponseBody r = response.successResponse("user pic updated", null);
            return new ResponseEntity<>(r, HttpStatus.OK);

        }catch (Exception e){
            ResponseBody r =response.catchHandler(e, "Error updating user pics: {} ");
          return new ResponseEntity<>(r, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

//TODO: refresh tokens, reset password, change email