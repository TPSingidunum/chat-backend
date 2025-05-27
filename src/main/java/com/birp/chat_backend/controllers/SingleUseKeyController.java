package com.birp.chat_backend.controllers;

import com.birp.chat_backend.dto.SingleUseKeyUploadDto;
import com.birp.chat_backend.security.SingleUseKeyService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/suk")
@AllArgsConstructor
public class SingleUseKeyController {

    private SingleUseKeyService singleUseKeyService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadDhKeys(@Valid @RequestBody SingleUseKeyUploadDto data) {
        try {
            singleUseKeyService.saveSingleUseKeys(data.getUserId(), data.getPublicKeys());
            return ResponseEntity.ok(Map.of("message", "Keys uploaded"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/next/{userId}")
    public ResponseEntity<?> getNextKey(@PathVariable int userId) {
        return singleUseKeyService.consumeFirstUnusedKey(userId)
                .<ResponseEntity<?>>map(k -> ResponseEntity.ok(Map.of("publicKey", k.getPublicKey())))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "No keys available")));
    }
}
