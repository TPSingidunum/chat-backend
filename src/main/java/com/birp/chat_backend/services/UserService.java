package com.birp.chat_backend.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.birp.chat_backend.models.User;
import com.birp.chat_backend.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> fetchAllUsers() {
        return this.userRepository.findAll();
    }
}
