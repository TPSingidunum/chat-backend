package rs.ac.singidunum.chat_backend.controllers;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.singidunum.chat_backend.dtos.crypto.OneTimePreKeyDto;
import rs.ac.singidunum.chat_backend.dtos.crypto.PreKeyBundleResponse;
import rs.ac.singidunum.chat_backend.dtos.crypto.PreKeyUploadRequest;
import rs.ac.singidunum.chat_backend.entities.OneTimePreKey;
import rs.ac.singidunum.chat_backend.entities.User;
import rs.ac.singidunum.chat_backend.repositories.OneTimePreKeyRepository;
import rs.ac.singidunum.chat_backend.repositories.UserRepository;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final OneTimePreKeyRepository oneTimePreKeyRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadKeys(@RequestHeader("X-Username") String username, @RequestBody PreKeyUploadRequest data){

        User user = new User();
        user.setUsername(username);
        user.setIdentityKey(data.getIdentityKey());
        user.setSignedPreKeyId(data.getSignedPreKeyId());
        user.setSignedPreKey(data.getSignedPreKey());
        user.setSignature(data.getSignature());
        user = userRepository.save(user);

        if (data.getKeys() != null) {
            for (OneTimePreKeyDto o : data.getKeys()) {
                OneTimePreKey otpk = new OneTimePreKey();
                otpk.setUserId(user.getUserId());
                otpk.setPreKeyId(o.getKeyId());
                otpk.setPreKey(o.getPublicKey());
                oneTimePreKeyRepository.save(otpk);
            }
        }

        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/bundle/{username}")
    @Transactional
    public ResponseEntity<?> getUserBundle(@PathVariable String username){

        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        PreKeyBundleResponse bundle = new PreKeyBundleResponse();
        bundle.setIdentityKey(user.get().getIdentityKey());
        bundle.setSignedPreKeyId(user.get().getSignedPreKeyId());
        bundle.setSignedPreKey(user.get().getSignedPreKey());
        bundle.setSignature(user.get().getSignature());

        Optional<OneTimePreKey> otpk =  oneTimePreKeyRepository.findByUserIdAndIsUsedFalseOrderByPreKeyIdAsc(user.get().getUserId());
        otpk.ifPresent(o -> {
            bundle.setOneTimePreKeyId(o.getPreKeyId());
            bundle.setOneTimePreKey(o.getPreKey());
            otpk.get().setUsed(true);
            oneTimePreKeyRepository.save(otpk.get());
        });

        return ResponseEntity.ok(bundle);
    }

    @GetMapping("/otpk/count")
    public ResponseEntity<?> getOneTimePreKeyCount(@RequestHeader("X-Username") String username){
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        int count = oneTimePreKeyRepository.countByUserIdAndIsUsedFalse(user.get().getUserId());

        return ResponseEntity.ok(Map.of("count", count));
    }
}
