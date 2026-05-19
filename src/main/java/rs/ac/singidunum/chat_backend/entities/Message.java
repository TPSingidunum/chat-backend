package rs.ac.singidunum.chat_backend.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="message")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="message_id")
    private int messageId;

    @Column(name="sender_id", nullable = false)
    private int senderId;

    @Column(name="receiver_id", nullable = false)
    private int receiverId;

    @Column(name="envelope", nullable = false)
    private String envelope;
}
