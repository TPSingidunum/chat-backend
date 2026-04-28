package rs.ac.singidunum.chat_backend.dtos.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendMessageResponse {
    private String from;
    private String payload;
}
