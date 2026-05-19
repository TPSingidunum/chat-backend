package rs.ac.singidunum.chat_backend.dtos.crypto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PreKeyBundleResponse {
    private String identityKey;
    private int signedPreKeyId;
    private String signedPreKey;
    private String signature;
    private int oneTimePreKeyId; // Mogu biti Null
    private String oneTimePreKey; // Mogu biti Null
}
