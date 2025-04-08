package com.birp.chat_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    @Order(1) // This filter chain gets evaluated first
    public SecurityFilterChain wsSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/ws/**") // Only apply to WebSocket URLs
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().permitAll()) // Allow all WebSocket connections
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin()));
        
        return http.build();
    }

    @Bean
    @Order(2) // This filter chain gets evaluated second
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**") // Only apply to API URLs
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/v1/session/otp", "/api/v1/login", "/api/v1/register").permitAll()
                .anyRequest().authenticated());
        
        return http.build();
    }
    
    @Bean
    @Order(3) // Fallback for any other URL
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().authenticated());
        
        return http.build();
    }
}
