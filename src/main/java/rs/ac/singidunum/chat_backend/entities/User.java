package rs.ac.singidunum.chat_backend.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="user")
@Data
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="user_id")
    private int userId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name="identity_key", nullable = false)
    private String identityKey;

    @Column(name="signed_pre_key_id", nullable = false)
    private int signedPreKeyId;

    @Column(name="signed_pre_key", nullable = false)
    private String signedPreKey;

    @Column(name="signature", nullable = false)
    private String signature;
}
