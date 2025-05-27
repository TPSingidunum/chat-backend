package com.birp.chat_backend.security;

import com.birp.chat_backend.models.SingleUseKey;
import com.birp.chat_backend.models.User;
import com.birp.chat_backend.repository.SingleUseKeyRepository;
import com.birp.chat_backend.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class SingleUseKeyService {

    private SingleUseKeyRepository singleUseKeyRepository;
    private UserRepository userRepository;

    public void saveSingleUseKeys(int userId, List<String> keys) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        User user = userOptional.get();
        for (String k : keys) {
            SingleUseKey suk = new SingleUseKey();
            suk.setUserId(user.getUserId());
            suk.setPublicKey(k);
            suk.setUsed(false);
            singleUseKeyRepository.save(suk);
        }
    }

    public Optional<SingleUseKey> consumeFirstUnusedKey(int userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return Optional.empty();
        }

        User user = userOptional.get();
        Optional<SingleUseKey> keyOpt = singleUseKeyRepository.findFirstByUserIdAndUsedFalse(user.getUserId());
        keyOpt.ifPresent(k -> {
            k.setUsed(true);
            singleUseKeyRepository.save(k);
        });
        return keyOpt;
    }
}
