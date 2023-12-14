package com.dolph.blog.dto.user;

import lombok.Data;

@Data
public class UserDataResponse {
    private String id;
    private String fullname;
    private String bio;
    private String twitter;
    private String pics;
    private String email;
    private String password;
    private String otp;
    private String otpExpiry;
    private boolean isEmailVerified;
    private String createdAt;
    private String updatedAt;


}
