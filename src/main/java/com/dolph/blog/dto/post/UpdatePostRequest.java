package com.dolph.blog.dto.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePostRequest {
    private String title;
    private String content;
    private String descr;
    private String category;
}
