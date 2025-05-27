package com.birp.chat_backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "single_use_key")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SingleUseKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "single_use_key_id")
    private int singleUseKeyId;

    @Column(name = "user_id")
    private int userId;

    @Column(name = "public_key", nullable = false, columnDefinition = "TEXT")
    private String publicKey;

    @Column(nullable = false)
    private boolean used = false;
}
