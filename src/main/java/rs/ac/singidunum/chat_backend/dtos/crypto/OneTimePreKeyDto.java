package rs.ac.singidunum.chat_backend.dtos.crypto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OneTimePreKeyDto {
    private int keyId;
    private String publicKey;
}
