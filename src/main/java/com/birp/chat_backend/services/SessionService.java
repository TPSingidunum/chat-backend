package com.birp.chat_backend.services;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.birp.chat_backend.dtos.UserLoginDto;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class SessionService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.ttl}")
    private int jwtTTL;

    private enum JwtType {
        access, refresh, otp
    };

    public String generateOtpToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", JwtType.otp);

        String tokenId = UUID.randomUUID().toString();

        return generateToken(claims, tokenId);
    }

    public String generateDefault(UserLoginDto user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("user_id", user.getUserId());
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());

        return generateToken(claims, user.getEmail());
    }

    public boolean verifyToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims extractAllClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts
                .parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String generateToken(Map<String, Object> extraClaims, String subject) {
        Date now = new Date(System.currentTimeMillis());
        Date expiry = new Date(System.currentTimeMillis() + jwtTTL * 3600 * 1000);
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }
}
