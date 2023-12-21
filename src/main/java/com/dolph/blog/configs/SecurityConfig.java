package com.dolph.blog.configs;

import com.dolph.blog.utils.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception{
       httpSecurity.authorizeHttpRequests((auth) -> auth.requestMatchers("/v1/auth/**")
                       .permitAll().anyRequest().authenticated())
               .addFilterAfter(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class).
               csrf(AbstractHttpConfigurer::disable).
               cors((httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configure(httpSecurity)));

//      httpSecurity.authorizeHttpRequests((auth) -> auth.requestMatchers("/v1/auth/**")
//              .permitAll().anyRequest().authenticated()).oauth2Login(Customizer.withDefaults())
//          .addFilterAfter(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class).
//          csrf(AbstractHttpConfigurer::disable).
//          cors((httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configure(httpSecurity)));
       return httpSecurity.build();
    }


    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(){
        return (web) -> web.ignoring().requestMatchers("/auth/**");
    }
}