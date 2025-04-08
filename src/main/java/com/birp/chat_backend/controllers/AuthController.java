package com.birp.chat_backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.birp.chat_backend.dto.UserRegistrationDto;
import com.birp.chat_backend.models.User;
import com.birp.chat_backend.services.SessionService;
import com.birp.chat_backend.services.UserService;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class AuthController {
   
    @Autowired
    private UserService userService;

    @Autowired
    private SessionService sessionService;

    @GetMapping("/session/otp")
    public ResponseEntity<Map<String, String>> getSessionOtp() {
        String token = sessionService.generateOtpToken();
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        // Check if user exists with this email
        if (userService.existsByEmail(registrationDto.getEmail())) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "User already exists with this email"));
        }
        
        // Create new user entity
        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());
        user.setCertificate(registrationDto.getCertificate());
        
        // Save the user
        User savedUser = userService.save(user);
        
        // Return successful registration response
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered successfully");
        response.put("user", savedUser);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
