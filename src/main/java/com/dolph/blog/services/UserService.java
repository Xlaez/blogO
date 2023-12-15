package com.dolph.blog.services;

import com.dolph.blog.dto.user.UserDataResponse;
import com.dolph.blog.helpers.TimestampUtil;
import com.dolph.blog.interfaces.UserProjection;
import com.dolph.blog.models.Token;
import com.dolph.blog.models.User;
import com.dolph.blog.repository.UserRepo;
import com.dolph.blog.utils.EmailSender;
import com.mongodb.client.result.UpdateResult;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    public boolean comparePassword(String text, String hashedPassword){
        Argon2PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        return  encoder.matches(text, hashedPassword);
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

    public Optional<User> getUserById(String id){
        User user = mongoTemplate.findOne(new Query(Criteria.where("_id").is(id)), User.class);
        return Optional.ofNullable(user);
    }

    public long deleteUserById(String userId){
        Query query = new Query(Criteria.where("_id").is(userId));
        return mongoTemplate.remove(query, User.class).getDeletedCount();
    }

    public UserProjection getUserByEmailProjection(String email){
        return userRepo.findUserProjectionByEmail(email);
    }

    public UserProjection getUserByIdProjection(String id){
        return userRepo.findUserProjectionById(id);
    }

    public UpdateResult updateUser(Query query, Update update){
        return mongoTemplate.updateFirst(query, update, User.class);
    }

    public Page<User> fetchUsers(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        return userRepo.findAll(pageable);
    }

    public void sendEmail(String recipient, String subject, Map<String, Object> variables) throws MessagingException {
        emailSender.sendEmail(recipient, subject, variables);
    }

    public UserDataResponse mapUserToUserDTO(User user) {
        UserDataResponse userDTO = new UserDataResponse();
        userDTO.setId(user.getId());
        userDTO.setFullname(user.getFullname());
        userDTO.setBio(user.getBio());
        userDTO.setTwitter(user.getTwitter());
        userDTO.setPics(user.getPics());
        userDTO.setEmail(user.getEmail());
        userDTO.setCreatedAt(user.getCreatedAt());
        userDTO.setEmailVerified(user.isEmailVerified());
        return userDTO;
    }
}
