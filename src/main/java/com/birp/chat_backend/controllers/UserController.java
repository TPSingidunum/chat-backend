package com.birp.chat_backend.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.birp.chat_backend.dto.UsernameDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.birp.chat_backend.models.User;
import com.birp.chat_backend.services.UserService;
import org.springframework.web.bind.annotation.GetMapping;

import javax.swing.text.html.Option;

@Controller
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = this.userService.fetchAllUsers();
        List<String> usernames = new ArrayList<>();

        for (User u : users) {
            usernames.add(u.getUsername());
        }

        return ResponseEntity.ok().body(usernames);
    }

    @GetMapping("/username")
    public ResponseEntity<?> getUserByUsername(@RequestBody UsernameDto data) {
        Optional<User> user = this.userService.fetchUserByUsername(data.getUsername());

        if (user.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "User with that username does not exist"));
        }

        return ResponseEntity.ok().body(user.get());
    }
}
