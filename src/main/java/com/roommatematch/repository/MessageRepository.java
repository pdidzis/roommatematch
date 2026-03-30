package com.roommatematch.repository;

import com.roommatematch.model.entity.ChatRoom;
import com.roommatematch.model.entity.Message;
import com.roommatematch.model.entity.User;
import com.roommatematch.model.enums.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByChatRoomOrderBySentAtAsc(ChatRoom chatRoom);

    List<Message> findByChatRoomIdOrderBySentAtAsc(Long chatRoomId);

    List<Message> findBySenderAndStatus(User sender, MessageStatus status);

    List<Message> findByChatRoomAndStatusNot(ChatRoom chatRoom, MessageStatus status);

    long countByChatRoomAndSenderNotAndStatus(ChatRoom chatRoom, User sender, MessageStatus status);
}
