package com.birp.chat_backend.services;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
        String tokenId = UUID.randomUUID().toString();
        Date now = new Date(System.currentTimeMillis());
        Date expiry = new Date(System.currentTimeMillis() + jwtTTL * 3600 * 1000);
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        String jwt = Jwts.builder()
        .id(tokenId)
        .issuedAt(now)
        .expiration(expiry)
        .claim("type", JwtType.otp)
        .signWith(key)
        .compact();

        return jwt;
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
}
