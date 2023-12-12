package com.dolph.blog.services.user;

import com.dolph.blog.helpers.OtpGenerator;
import com.dolph.blog.models.User;
import com.dolph.blog.repository.UserRepo;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class UserService {
    private final UserRepo userRepo;

    public UserService(UserRepo userRepo){
        this.userRepo = userRepo;
    }

    public String getTimestamp(){
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return currentDateTime.format(formatter);
    }

    public String hashPassword(String text){
        Argon2PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        String result = encoder.encode(text);
        return result;
    }

    public String createUser(com.dolph.blog.dto.user.NewUserRequest newUserRequest){
       try{
           User user = User.builder()
                   .fullname(newUserRequest.getFullname())
                   .bio(newUserRequest.getBio())
                   .email(newUserRequest.getEmail())
                   .password(hashPassword(newUserRequest.getPassword()))
                   .createdAt(getTimestamp())
                   .updatedAt(getTimestamp())
                   .build();
           user.setOtp(OtpGenerator.newOtp());
           this.userRepo.save(user);
           return user.getId();
       }catch(Exception e){
           throw e;
       }
    }
}
