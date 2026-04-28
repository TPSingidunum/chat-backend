package rs.ac.singidunum.chat_backend.websocket;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConnectedUsers {

    private final Map<String, String> usersBySessionId = new ConcurrentHashMap<>();

    public void register(String sessionId, String username) {
        usersBySessionId.put(sessionId,username);
    }

    public String getUsername(String sessionId) {
        return usersBySessionId.getOrDefault(sessionId, "TEMP");
    }

    public void remove(String sessionId) {
        usersBySessionId.remove(sessionId);
    }

    public List<String> getConnectedUsers() {
        return usersBySessionId.values().stream().distinct().sorted().toList();
    }
}
