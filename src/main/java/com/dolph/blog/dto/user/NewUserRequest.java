package com.dolph.blog.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewUserRequest {
    @Id
    private String id;
    private String fullname;
    private String bio;
    private String email;
    private String password;
    private String otp;
    private String otpExpiry;
}
