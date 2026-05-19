package rs.ac.singidunum.chat_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.singidunum.chat_backend.entities.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
}
