package com.dolph.blog.utils;

import com.dolph.blog.dto.user.ResponseBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Slf4j
public class ErrorHandler {

    public static ResponseBody catchHandler(Exception e, String message){
        log.error(message, e);
        ResponseBody response = new ResponseBody();
        response.setStatus("error");
        response.setMessage(e.getMessage());
        return response;
    }
}
