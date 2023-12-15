package com.dolph.blog.dto.post;

import lombok.Data;

@Data
public class PostDataResponse {
    private String id;
    private String title;
    private String content;
    private String img;
    private String descr;
    private String category;
    private String authorId;
    private String createdAt;
    private String updatedAt;
}
