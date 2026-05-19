package rs.ac.singidunum.chat_backend.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="one_time_pre_key")
@Data
@NoArgsConstructor
public class OneTimePreKey {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="one_time_pre_key_id")
    private int oneTimePreKeyId;

    @Column(name = "user_id", nullable = false)
    private int userId;

    @Column(name = "pre_key_id", nullable = false)
    private int preKeyId;

    @Column(name = "pre_key", nullable = false)
    private String preKey;

    @Column(name = "is_used", nullable = false)
    private boolean isUsed = false;

}
