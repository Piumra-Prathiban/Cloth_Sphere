package com.clothsphere.repository.CPM;

import com.clothsphere.model.CPM.MessageResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageResponseRepository extends JpaRepository<MessageResponse, String> {

    // Find all responses for a specific message
    List<MessageResponse> findByMessageId(String messageId);

    // Find all responses by a specific officer
    List<MessageResponse> findByRespondedBy(String respondedBy);

    // Find responses for a message ordered by response date
    List<MessageResponse> findByMessageIdOrderByResponseDateAsc(String messageId);

    // Count responses for a specific message
    long countByMessageId(String messageId);
}
