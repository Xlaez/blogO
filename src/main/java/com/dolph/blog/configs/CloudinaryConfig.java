package com.dolph.blog.configs;

import com.cloudinary.Cloudinary;
import com.dolph.blog.utils.FileUploader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Value("${spring.cloudinary.name}")
    private String cloudName;

    @Value("${spring.cloudinary.apiKey}")
    private String apikey;

    @Value("${spring.cloudinary.apiSecret}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinaryConfiguration(){
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apikey);
        config.put("api_secret", apiSecret);
        return new Cloudinary(config);
    }

    @Bean
    public FileUploader fileUploader(Cloudinary cloudinary){
        return new FileUploader(cloudinary);
    }

}
