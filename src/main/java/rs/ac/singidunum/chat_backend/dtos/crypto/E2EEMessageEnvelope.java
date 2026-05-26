package rs.ac.singidunum.chat_backend.dtos.crypto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class E2EEMessageEnvelope {
    private Integer receiverId;
    private Integer senderId;
    // Serializovan objecat (bytes, ili JSON)
    private String envelope;
    private boolean initialMessage; // Initial Key for user in Ratchet
    private Integer singedPreKey;
    private Integer oneTimePreKeyId;
    private String  userIdentityKey;
    private String userEphemeralKey;
}
