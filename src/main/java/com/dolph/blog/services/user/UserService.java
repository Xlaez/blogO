package com.dolph.blog.services.user;

import com.dolph.blog.helpers.TimestampUtil;
import com.dolph.blog.models.User;
import com.dolph.blog.repository.UserRepo;
import com.dolph.blog.utils.EmailSender;
import com.mongodb.client.result.UpdateResult;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepo userRepo;
    private final MongoTemplate mongoTemplate;

    @Autowired
    private EmailSender emailSender;
    public UserService(UserRepo userRepo, MongoTemplate mongoTemplate){
        this.userRepo = userRepo;
        this.mongoTemplate = mongoTemplate;
    }

    public String hashPassword(String text){
        Argon2PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        return encoder.encode(text);
    }

    public String createUser(com.dolph.blog.dto.user.NewUserRequest newUserRequest){
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
    }

    public Optional<User> getUserByEmail(String email){
        User user = mongoTemplate.findOne(new Query(Criteria.where("email").is(email)), User.class);
        return Optional.ofNullable(user);
    }

    public UpdateResult updateUser(Query query, Update update){
        return mongoTemplate.updateFirst(query, update, User.class);
    }

    public void sendEmail(String recipient, String subject, Map<String, Object> variables) throws MessagingException {
        emailSender.sendEmail(recipient, subject, variables);
    }
}
