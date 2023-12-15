package com.dolph.blog.utils;

import com.dolph.blog.dto.user.ResponseBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

import java.util.Map;

@Slf4j
public class ApiResponse {

    private ResponseBody response;

    public ApiResponse(){
        response = new ResponseBody();
    }

    public ResponseBody successResponse(String m, @Nullable Map body){
        response.setStatus("success");
        response.setMessage(m);
        if(body != null){
            response.setBody(body);
        }
        return response;
    }

    public ResponseBody failureResponse(String m,@Nullable Map body){
        response.setStatus("failure");
        response.setMessage(m);
        if(body != null){
            response.setBody(body);
        }
        return response;
    }

    public ResponseBody catchHandler(Exception e, String m){
        log.error(m, e);
        response.setStatus("error");
        response.setMessage(e.getMessage());
        return response;
    }
}
