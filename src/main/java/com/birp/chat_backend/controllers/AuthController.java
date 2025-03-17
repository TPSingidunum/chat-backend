package com.birp.chat_backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.birp.chat_backend.services.SessionService;
import com.birp.chat_backend.services.UserService;

@Controller
@RequestMapping("/api/v1")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private SessionService sessionService;

   @GetMapping("/session/otp")  // Ensure path matches exactly
    public ResponseEntity<String> getSessionTokenOtp() {
        return ResponseEntity.ok(sessionService.generateOtpToken());
    }
    

}
