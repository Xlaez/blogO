package com.dolph.blog.dto.user;

import lombok.Data;

import java.util.Map;

@Data
public class ResponseBody {
    private String status;
    private String message;
    private Map body;
}
