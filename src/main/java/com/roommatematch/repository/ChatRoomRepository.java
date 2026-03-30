package com.roommatematch.repository;

import com.roommatematch.model.entity.ChatRoom;
import com.roommatematch.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByParticipant1AndParticipant2(User p1, User p2);

    Optional<ChatRoom> findByParticipant2AndParticipant1(User p2, User p1);

    List<ChatRoom> findByParticipant1OrParticipant2(User p1, User p2);
}
