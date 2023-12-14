package com.dolph.blog.services;

import com.dolph.blog.helpers.TimestampUtil;
import com.dolph.blog.models.Token;
import com.dolph.blog.repository.TokenRepo;
import com.dolph.blog.utils.JwtUtil;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service

public class TokenService {

    private final TokenRepo tokenRepo;
    private final MongoTemplate mongoTemplate;

    public TokenService(TokenRepo tokenRepo,MongoTemplate mongoTemplate){
        this.mongoTemplate = mongoTemplate;
        this.tokenRepo = tokenRepo;
    }

    public String saveToken(com.dolph.blog.dto.user.TokenRequest tokenRequest){
        Token token = Token.builder()
                .token(tokenRequest.getToken())
                .expires(tokenRequest.getExpires())
                .userId(tokenRequest.getUserId())
                .createdAt(TimestampUtil.getTimestamp())
                .updatedAt(TimestampUtil.getTimestamp())
                .build();
        this.tokenRepo.save(token);
        return token.getExpires();
    }

    public Map<String, Object> generateAuthTokens(String userId) {
        String accessToken = JwtUtil.buildToken(userId, "access");
        String refreshToken = JwtUtil.buildToken(userId, "refresh");

        Map<String, Object> tokens = new HashMap<>();

        Map<String, Object> accessTokenDoc =  new HashMap<>();
        accessTokenDoc.put("token", accessToken);
        accessTokenDoc.put("expires", JwtUtil.getExpirationTime(accessToken));

        Map<String, Object> refreshTokenDoc = new HashMap<>();
        refreshTokenDoc.put("token", refreshToken);
        refreshTokenDoc.put("expires", JwtUtil.getExpirationTime(refreshToken));

        tokens.put("access", accessTokenDoc);
        tokens.put("refresh", refreshTokenDoc);

        return tokens;
    }
}
