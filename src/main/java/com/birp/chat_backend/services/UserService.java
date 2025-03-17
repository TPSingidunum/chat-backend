package com.birp.chat_backend.services;

import java.util.List;
import java.util.Optional;

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

    public Optional<User> fetchUserById(int userId) {
        return this.userRepository.findById(userId);
    }

    public boolean existsByEmail(String email) {
        return this.userRepository.existsByEmail(email);
    }

    public User save(User user) {
        return this.userRepository.save(user);
    }
}
