package com.dolph.blog.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(value = "token")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Token {
    private String token;
    private String expires;
    private String userId;

    private String createdAt;
    private String updatedAt;
}
