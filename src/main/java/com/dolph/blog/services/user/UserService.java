package com.dolph.blog.services.user;

import com.dolph.blog.helpers.OtpGenerator;
import com.dolph.blog.helpers.TimestampUtil;
import com.dolph.blog.models.User;
import com.dolph.blog.repository.UserRepo;
import com.dolph.blog.utils.EmailSender;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class UserService {
    private final UserRepo userRepo;

    @Autowired
    private EmailSender emailSender;
    public UserService(UserRepo userRepo){
        this.userRepo = userRepo;
    }

    public String hashPassword(String text){
        Argon2PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        return encoder.encode(text);
    }

    public String createUser(com.dolph.blog.dto.user.NewUserRequest newUserRequest){
       try{
           User user = User.builder()
                   .fullname(newUserRequest.getFullname())
                   .bio(newUserRequest.getBio())
                   .email(newUserRequest.getEmail())
                   .password(hashPassword(newUserRequest.getPassword()))
                   .otp(newUserRequest.getOtp())
                   .otpExpiry(newUserRequest.getOtpExpiry())
                   .isEmailVerified(false)
                   .createdAt(TimestampUtil.getTimestamp())
                   .updatedAt(TimestampUtil.getTimestamp())
                   .build();
           this.userRepo.save(user);
           return user.getId();
       }catch(Exception e){
           throw e;
       }
    }

    public void sendEmail(String recipient, String subject, Map<String, Object> variables) throws MessagingException {
        emailSender.sendEmail(recipient, subject, variables);
    }
}
