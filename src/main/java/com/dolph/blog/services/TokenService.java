package com.dolph.blog.services;

import com.dolph.blog.helpers.TimestampUtil;
import com.dolph.blog.models.Token;
import com.dolph.blog.models.User;
import com.dolph.blog.repository.TokenRepo;
import com.dolph.blog.utils.JwtUtil;
import com.mongodb.client.result.DeleteResult;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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

    public Map<String, Map<String , Object>> generateAuthTokens(String userId) {
        String accessToken = JwtUtil.buildToken(userId, "access");
        String refreshToken = JwtUtil.buildToken(userId, "refresh");

        Map<String, Map<String , Object>> tokens = new HashMap<>();

        Map<String, Object> accessTokenDoc =  new HashMap<>();
        accessTokenDoc.put("token", accessToken);
        accessTokenDoc.put("expires", JwtUtil.getExpirationTime(accessToken));

        Map<String, Object> refreshTokenDoc = new HashMap<>();
        refreshTokenDoc.put("token", refreshToken);
        refreshTokenDoc.put("expires", JwtUtil.getExpirationTime(refreshToken));

        tokens.put("accessToken", accessTokenDoc);
        tokens.put("refreshToken", refreshTokenDoc);

        return tokens;
    }

    public DeleteResult deleteToken(String refreshToken){
        Query query = new Query(Criteria.where("token").is(refreshToken));
        return mongoTemplate.remove(query, Token.class);
    }
}
