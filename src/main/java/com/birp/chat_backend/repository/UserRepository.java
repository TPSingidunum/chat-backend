package com.birp.chat_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.birp.chat_backend.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>{
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
}
