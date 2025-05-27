package com.birp.chat_backend.repository;

import com.birp.chat_backend.models.SingleUseKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SingleUseKeyRepository extends JpaRepository<SingleUseKey, Integer> {
    Optional<SingleUseKey> findFirstByUserIdAndUsedFalse(int userId);
}
