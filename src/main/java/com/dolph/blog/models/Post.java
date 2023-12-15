package com.dolph.blog.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(value = "post")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Post {
    @Id
    private String id;
    private String authorId;
    private String title;
    private String content;
    private String descr;
    private String img;
    private boolean published;
    private String category;
    private String createdAt;
    private String updatedAt;
}
