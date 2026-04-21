package rs.ac.singidunum.chat_backend.dtos.chat;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private boolean result;
    private String text;
}
