package com.dolph.blog.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {
    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS512); // move to application.properties
    private static final long ACCESS_EXPIRATION_TIME = 86400000; // move to application.properties - 24hrs in milliseconds

    private static final long REFRESH_EXPIRATION_TIME = 604800000; // move to application.properties - 24hrs in milliseconds

    public static String buildToken(String id, String type) {
        Date expirationTime = new Date((System.currentTimeMillis()
                + (type.equals("access") ? ACCESS_EXPIRATION_TIME : REFRESH_EXPIRATION_TIME)));

        return Jwts.builder().subject(id)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(Date.from(expirationTime.toInstant()))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY).compact();
    }

    public static String getIdFromToken(String token){
        try {
            Jws<Claims> jws = Jwts.parser().setSigningKey(SECRET_KEY).build().parseSignedClaims(token);
            Claims claims = jws.getPayload();
            return claims.getSubject();
        }catch(Exception e){
            throw e;
        }
    }

    public static Date getExpirationTime(String token){
        try {
            Jws<Claims> jws = Jwts.parser().setSigningKey(SECRET_KEY).build().parseSignedClaims(token);
            Claims claims = jws.getPayload();

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