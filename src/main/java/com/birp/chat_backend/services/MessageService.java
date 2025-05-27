package com.birp.chat_backend.services;

import com.birp.chat_backend.models.Message;
import com.birp.chat_backend.models.User;
import com.birp.chat_backend.repository.MessageRepository;
import com.birp.chat_backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    private MessageRepository messageRepository;
    private UserRepository userRepository;

    public void saveMessage(int senderId, int recipientId, String content) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found"));

        Message message = new Message();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(content);
        message.setDelivered(false);
        messageRepository.save(message);
    }

    public List<Message> consumeUndeliveredMessages(int userId) {
        User recipient = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found"));
        List<Message> messages = messageRepository.findByRecipientAndDeliveredFalse(recipient);
        for (Message m : messages) {
            m.setDelivered(true);
            messageRepository.save(m);
        }
        return messages;
    }
}