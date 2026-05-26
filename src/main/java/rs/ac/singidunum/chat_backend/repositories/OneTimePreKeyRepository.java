package rs.ac.singidunum.chat_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.ac.singidunum.chat_backend.entities.OneTimePreKey;

import java.util.Optional;

@Repository
public interface OneTimePreKeyRepository extends JpaRepository<OneTimePreKey, Integer> {

    Optional<OneTimePreKey> findByUserIdAndIsUsedFalseOrderByPreKeyIdAsc(int userId);
    int countByUserIdAndIsUsedFalse(int userId);
}
