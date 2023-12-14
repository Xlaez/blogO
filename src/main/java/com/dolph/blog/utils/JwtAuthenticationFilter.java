package com.dolph.blog.utils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Map tokens = extractToken(request);

        if(tokens != null){
            String accessToken = String.valueOf(tokens.get("access_token"));
            String refreshToken = String.valueOf(tokens.get("refresh_token"));

            if (accessToken != null && JwtUtil.validateToken(accessToken)) {
                String id = JwtUtil.getIdFromToken(accessToken);
                SecurityContextHolder.getContext().setAuthentication(new JwtAuthorization(id));
            }
        }

        // TODO : parse refresh tokens

        filterChain.doFilter(request, response);
    }

    private Map extractToken(HttpServletRequest request){
        Map<String, String> tokens;

        tokens = new HashMap<>();
        String bearerToken = request.getHeader("x-auth-token");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            tokens.put("access_token", bearerToken.substring(7));
        }
        String refreshToken = request.getHeader("x-refresh-token");
        if(refreshToken !=null && refreshToken.startsWith("Refresh ")){
            tokens.put("refresh_token", refreshToken.substring(8));
        }

        if(tokens.isEmpty()){
            return null;
        }else{
            return tokens;
        }
    }
}