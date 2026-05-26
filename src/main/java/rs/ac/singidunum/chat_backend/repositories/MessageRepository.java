package rs.ac.singidunum.chat_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.ac.singidunum.chat_backend.entities.Message;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
    List<Message> findByReceiverIdOrderByCreatedAtAsc(int receiverId);
    void deleteByReceiverId(int userId);
}
