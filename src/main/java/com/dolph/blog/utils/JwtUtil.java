package com.dolph.blog.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class JwtUtil {
    private static final String SECRET_KEY = "very-secret-key-000-111"; // move ot env
    private static final long ACCESS_EXPIRATION_TIME = 86400000; // move to env - 24hrs in milliseconds

    private static final long REFRESH_EXPIRATION_TIME = 604800000; // move to env - 24hrs in milliseconds

    public static String buildToken(String id, String type) throws Exception {
        if (!type.equals("refresh") || !type.equals("access")){
            throw new Exception("jwt tokens can only be of type 'access' or 'refresh'");
        }
        Date expirationTime = new Date(System.currentTimeMillis()
                + type == "access" ? ACCESS_EXPIRATION_TIME : REFRESH_EXPIRATION_TIME);
        return Jwts.builder().subject(id).expiration(expirationTime)
                .issuedAt(new Date(System.currentTimeMillis()))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY).compact();
    }

    public static String getIdFromToken(String token){
        try {
            Claims claims = (Claims) Jwts.parser().setSigningKey(SECRET_KEY).build().parseSignedClaims(token);
            return claims.getSubject();
        }catch(Exception e){
            throw e;
        }
    }

    public static Date getExpirationTime(String token){
        try {
            Claims claims = (Claims) Jwts.parser().setSigningKey(SECRET_KEY).build().parseSignedClaims(token);
            return claims.getExpiration();
        }catch(Exception e){
            throw e;
        }
    }

    public static boolean validateToken(String token){
        try{
            Jwts.parser().setSigningKey(SECRET_KEY).build().parseSignedClaims(token);
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
