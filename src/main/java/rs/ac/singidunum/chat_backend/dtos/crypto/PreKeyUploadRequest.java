package rs.ac.singidunum.chat_backend.dtos.crypto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreKeyUploadRequest {
    private String identityKey;
    private int signedPreKeyId;
    private String signedPreKey;
    private String signature;
    private List<OneTimePreKeyDto> keys;
}
