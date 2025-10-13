package com.clothsphere.repository.CPM;

import com.clothsphere.model.CPM.MessageResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageResponseRepository extends JpaRepository<MessageResponse, String> {


    @Query("SELECT mr FROM MessageResponse mr WHERE mr.messageId = :messageId")
    List<MessageResponse> findByMessageId(@Param("messageId") String messageId);

    @Query("SELECT mr FROM MessageResponse mr WHERE mr.respondedBy = :respondedBy")
    List<MessageResponse> findByRespondedBy(@Param("respondedBy") String respondedBy);

    @Query("SELECT mr FROM MessageResponse mr WHERE mr.messageId = :messageId ORDER BY mr.responseDate ASC")
    List<MessageResponse> findByMessageIdOrderByResponseDateAsc(@Param("messageId") String messageId);

    @Query("SELECT COUNT(mr) FROM MessageResponse mr WHERE mr.messageId = :messageId")
    long countByMessageId(@Param("messageId") String messageId);
}
