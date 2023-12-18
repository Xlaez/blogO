package com.dolph.blog.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewCommentRequest {

    @Id
    private String id;
    private String text;
    private String userId;
    private String postId;
}
