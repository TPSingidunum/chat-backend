package com.birp.chat_backend.websocket;

import java.io.IOException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.birp.chat_backend.dto.websocket.AuthenticationResult;
import com.birp.chat_backend.dto.websocket.ChallengeMessage;
import com.birp.chat_backend.dto.websocket.ChallengeResponse;
import com.birp.chat_backend.models.User;
import com.birp.chat_backend.services.SessionService;
import com.birp.chat_backend.services.UserService;
import com.birp.chat_backend.utils.CertificateUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthenticationWebSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationWebSocketHandler.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserService userService;
    private final SessionService sessionService;
    private final CertificateUtil certificateUtil;
    
    // Store session challenges for verification
    // Redis server (Key-Value), mysql server(?)
    private final Map<String, String> sessionChallenges = new ConcurrentHashMap<>();
    private final Map<String, String> sessionEmails = new ConcurrentHashMap<>();
    private final Map<String, Boolean> authenticatedSessions = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void init() {
        certificateUtil.init(); // Ensure certificate util is initialized
    }
    
    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        logger.info("WebSocket connection established: {}", session.getId());
    }
    
    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        String payload = message.getPayload();
        logger.debug("Received raw message: {}", payload);
        
        try {
            // Parse the message as a JSON object
            JsonNode jsonPayload = objectMapper.readTree(payload);
            
            // Get the message type
            if (jsonPayload.has("type")) {
                String messageType = jsonPayload.get("type").asText();
                
                switch (messageType) {
                    case "AUTH_REQUEST":
                        handleAuthRequest(session, jsonPayload);
                        break;
                    case "CHALLENGE_RESPONSE":
                        handleChallengeResponse(session, payload);
                        break;
                    default:
                        logger.warn("Unknown message type: {}", messageType);
                        if (isAuthenticated(session)) {
                            handleAuthenticatedMessage(session, payload);
                        } else {
                            sendErrorAndClose(session, "Authentication required");
                        }
                }
            } else {
                logger.warn("Message has no type field: {}", payload);
                if (isAuthenticated(session)) {
                    handleAuthenticatedMessage(session, payload);
                } else {
                    sendErrorAndClose(session, "Authentication required");
                }
            }
        } catch (Exception e) {
            logger.error("Error parsing message: {}", e.getMessage());
            sendErrorAndClose(session, "Invalid message format");
        }
    }
    
    private void handleAuthRequest(WebSocketSession session, JsonNode payload) throws Exception {
        String email = payload.get("email").asText();
        
        logger.info("Handling auth request for email: {}", email);
        
        // Store email for this session
        sessionEmails.put(session.getId(), email);
        
        // Try to find user by email
        Optional<User> userOptional = userService.fetchUserByEmail(email);
        if (userOptional.isEmpty()) {
            sendErrorAndClose(session, "User not found");
            return;
        }
        
        User user = userOptional.get();
        String certificate = user.getCertificate();
        
        if (certificate == null || certificate.isEmpty()) {
            sendErrorAndClose(session, "User certificate not found");
            return;
        }
        
        try {
            // Extract public key from user's certificate
            PublicKey publicKey = certificateUtil.extractPublicKeyFromCertificate(certificate);
            
            // Generate random challenge
            byte[] randomBytes = new byte[32];
            new SecureRandom().nextBytes(randomBytes);
            String challenge = Base64.getEncoder().encodeToString(randomBytes);
            
            logger.debug("Generated challenge for session {}: {}", session.getId(), challenge);
            
            // Store challenge for verification later
            sessionChallenges.put(session.getId(), challenge);
            
            // Encrypt challenge with user's public key
            String encryptedChallenge = certificateUtil.encryptWithPublicKey(challenge, publicKey);
            
            // Send challenge to client
            ChallengeMessage challengeMessage = new ChallengeMessage();
            challengeMessage.setEncryptedChallenge(encryptedChallenge);
            
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(challengeMessage)));
            logger.info("Challenge sent to client session: {}", session.getId());
            
        } catch (Exception e) {
            logger.error("Error processing certificate: {}", e.getMessage(), e);
            sendErrorAndClose(session, "Error processing certificate: " + e.getMessage());
        }
    }
    
    private void handleChallengeResponse(WebSocketSession session, String payload) throws Exception {
        ChallengeResponse response = objectMapper.readValue(payload, ChallengeResponse.class);
        
        String sessionId = session.getId();
        String expectedChallenge = sessionChallenges.get(sessionId);
        String email = sessionEmails.get(sessionId);
        
        logger.info("Handling challenge response for session: {}", sessionId);
        
        if (expectedChallenge == null || email == null) {
            sendErrorAndClose(session, "Authentication sequence error");
            return;
        }
        
        boolean verified = false;
        try {
            // Decrypt the response using server's private key
            String decryptedResponse = certificateUtil.decryptWithServerPrivateKey(response.getEncryptedResponse());
            
            // Verify it matches the original challenge
            verified = expectedChallenge.equals(decryptedResponse);
            
            logger.info("Challenge verification result: {}", verified);
        } catch (Exception e) {
            logger.error("Error verifying challenge: {}", e.getMessage(), e);
            sendErrorAndClose(session, "Error verifying challenge: " + e.getMessage());
            return;
        }
        
        if (verified) {
            // Mark session as authenticated
            authenticatedSessions.put(sessionId, true);
            
            // Generate JWT token for subsequent HTTP requests
            Optional<User> userOptional = userService.fetchUserByEmail(email);
            if (userOptional.isEmpty()) {
                sendErrorAndClose(session, "User not found");
                return;
            }
            
            User user = userOptional.get();
            // Generate token using the new method directly with User object
            String token = sessionService.generateUserToken(user);
            
            // Send success response with token
            AuthenticationResult result = new AuthenticationResult();
            result.setSuccess(true);
            result.setMessage("Authentication successful");
            result.setToken(token);
            
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(result)));
            logger.info("Authentication successful for session: {}", sessionId);
        } else {
            sendErrorAndClose(session, "Challenge verification failed");
        }
        
        // Clean up challenge data
        sessionChallenges.remove(sessionId);
    }
    
    private void handleAuthenticatedMessage(WebSocketSession session, String payload) throws IOException {
        //TODO: Implement real message storage
        System.out.println("Authenticated message received: " + payload);
        logger.info("Received authenticated message from session {}: {}", session.getId(), payload);

            Map<String, String> msg = Map.of(
                    "type", "MESSAGE_RECIEVED",
                    "message", "Messsage has been recieved"
            );

            String json = objectMapper.writeValueAsString(msg);
        session.sendMessage(new TextMessage(json));
    }
    
    private boolean isAuthenticated(WebSocketSession session) {
        Boolean authenticated = authenticatedSessions.get(session.getId());
        return authenticated != null && authenticated;
    }
    
    private void sendErrorAndClose(WebSocketSession session, String errorMessage) throws IOException {
        //TODO: Shouldn't be just auth result, make it CustomResult
        AuthenticationResult result = new AuthenticationResult();
        result.setSuccess(false);
        result.setMessage(errorMessage);
        
        logger.error("Authentication error: {}", errorMessage);
        
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(result)));
    }
    
    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        logger.info("WebSocket connection closed: {} with status: {}", session.getId(), status);
        
        // Clean up
        String sessionId = session.getId();
        sessionChallenges.remove(sessionId);
        sessionEmails.remove(sessionId);
        authenticatedSessions.remove(sessionId);
    }
}