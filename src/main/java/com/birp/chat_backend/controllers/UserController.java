package com.birp.chat_backend.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.birp.chat_backend.models.User;
import com.birp.chat_backend.services.UserService;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = this.userService.fetchAllUsers();

        return ResponseEntity.ok().body(users);
    }
}
