package com.birp.chat_backend.repository;

import com.birp.chat_backend.models.Message;
import com.birp.chat_backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Integer> {
    List<Message> findByRecipientAndDeliveredFalse(User recipient);
}
