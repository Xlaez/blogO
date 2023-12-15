package com.dolph.blog.dto.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewPostRequest {
    @Id
    private String id;
    private String title;
    private String content;
    private String img;
    private String descr;
    private String category;
    private String authorId;
}
