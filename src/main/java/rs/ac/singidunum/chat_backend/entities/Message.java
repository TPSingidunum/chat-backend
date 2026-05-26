package rs.ac.singidunum.chat_backend.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name="message")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="message_id")
    private Integer messageId;

    @Column(name="sender_id", nullable = false)
    private int senderId;

    @Column(name="receiver_id", nullable = false)
    private Integer receiverId;

    @Column(name="envelope", nullable = false)
    private String envelope;

    @Column(name="created_at")
    private LocalDateTime createdAt;

}
