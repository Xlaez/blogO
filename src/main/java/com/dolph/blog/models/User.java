package com.dolph.blog.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(value = "user")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class User {
    @Id
    private String id;
    private String fullname;
    private String bio;
    private String twitter;
    private String pics;
    private String email;
    private String password;

    private String createdAt;
    private String updatedAt;
}
