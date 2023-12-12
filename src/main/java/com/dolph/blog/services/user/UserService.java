package com.dolph.blog.services.user;

import com.dolph.blog.models.User;
import com.dolph.blog.repository.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class UserService {
    private final UserRepo userRepo;

    public UserService(UserRepo userRepo){
        this.userRepo = userRepo;
    }

    public String getTimestamp(){
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = currentDateTime.format(formatter);
        return formattedDateTime;
    }

    public void createUser(com.dolph.blog.dto.user.NewUserRequest newUserRequest){
        User user = User.builder().fullname(newUserRequest.getFullname())
                .bio(newUserRequest.getBio()).email(newUserRequest.getEmail())
                .password(newUserRequest.getPassword()).createdAt(getTimestamp()).updatedAt(getTimestamp())
                .build();
        this.userRepo.save(user);
        log.info("User {} has been created", user.getId());
    }
}
