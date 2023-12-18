package com.dolph.blog.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(value= "comment")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Comment {
    @Id
    private String id;
    private String text;
    private String userId;
    private String parentId;
    private String replyCount;
    private String totalLikes;
    private String postId;
    private String createdAt;
    private String updatedAt;
}
