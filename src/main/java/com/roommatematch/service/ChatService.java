package com.roommatematch.service;

import com.roommatematch.dto.response.ChatRoomResponse;
import com.roommatematch.dto.response.MessageResponse;
import com.roommatematch.dto.response.UserSummary;
import com.roommatematch.exception.BusinessLogicException;
import com.roommatematch.exception.ResourceNotFoundException;
import com.roommatematch.exception.UnauthorizedException;
import com.roommatematch.model.entity.ChatRoom;
import com.roommatematch.model.entity.Match;
import com.roommatematch.model.entity.Message;
import com.roommatematch.model.entity.User;
import com.roommatematch.model.enums.MatchStatus;
import com.roommatematch.model.enums.MessageStatus;
import com.roommatematch.repository.ChatRoomRepository;
import com.roommatematch.repository.MatchRepository;
import com.roommatematch.repository.MessageRepository;
import com.roommatematch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public ChatRoomResponse getOrCreateChatRoom(Long userId1, Long userId2) {
        User user1 = userRepository.findById(userId1)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId1));
        User user2 = userRepository.findById(userId2)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId2));

        // Check if accepted match exists between users
        List<Match> matches = matchRepository.findByRequesterOrReceiver(user1, user1);
        boolean hasAcceptedMatch = matches.stream()
                .anyMatch(match ->
                        match.getStatus() == MatchStatus.ACCEPTED &&
                        ((match.getRequester().getId().equals(userId1) && match.getReceiver().getId().equals(userId2)) ||
                         (match.getRequester().getId().equals(userId2) && match.getReceiver().getId().equals(userId1)))
                );

        if (!hasAcceptedMatch) {
            throw new BusinessLogicException("You can only chat with accepted matches");
        }

        // Check if chat room already exists
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByParticipant1AndParticipant2(user1, user2);
        if (existingRoom.isEmpty()) {
            existingRoom = chatRoomRepository.findByParticipant2AndParticipant1(user1, user2);
        }

        ChatRoom chatRoom;
        if (existingRoom.isPresent()) {
            chatRoom = existingRoom.get();
        } else {
            chatRoom = ChatRoom.builder()
                    .participant1(user1)
                    .participant2(user2)
                    .build();
            chatRoom = chatRoomRepository.save(chatRoom);
            log.info("Chat room created between users {} and {}", userId1, userId2);
        }

        List<Message> messages = messageRepository.findByChatRoomOrderBySentAtAsc(chatRoom);
        return mapToChatRoomResponse(chatRoom, user1, messages);
    }

    @Transactional
    public MessageResponse sendMessage(Long senderId, Long chatRoomId, String content) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User", senderId));

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom", chatRoomId));

        // Validate sender is a participant
        boolean isParticipant = chatRoom.getParticipant1().getId().equals(senderId) ||
                                chatRoom.getParticipant2().getId().equals(senderId);

        if (!isParticipant) {
            throw new UnauthorizedException("You are not a participant in this chat room");
        }

        Message message = Message.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .content(content)
                .status(MessageStatus.SENT)
                .build();

        Message savedMessage = messageRepository.save(message);
        log.info("Message sent in room {} by user {}", chatRoomId, senderId);

        return mapToMessageResponse(savedMessage, senderId);
    }

    @Transactional
    public List<MessageResponse> getChatHistory(Long userId, Long chatRoomId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom", chatRoomId));

        // Validate user is a participant
        boolean isParticipant = chatRoom.getParticipant1().getId().equals(userId) ||
                                chatRoom.getParticipant2().getId().equals(userId);

        if (!isParticipant) {
            throw new UnauthorizedException("You are not a participant in this chat room");
        }

        List<Message> messages = messageRepository.findByChatRoomOrderBySentAtAsc(chatRoom);

        // Mark unread messages as READ
        for (Message message : messages) {
            if (!message.getSender().getId().equals(userId) && message.getStatus() != MessageStatus.READ) {
                message.setStatus(MessageStatus.READ);
                messageRepository.save(message);
            }
        }

        return messages.stream()
                .map(message -> mapToMessageResponse(message, userId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getMyChatRooms(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        List<ChatRoom> chatRooms = chatRoomRepository.findByParticipant1OrParticipant2(user, user);

        List<ChatRoomResponse> responses = new ArrayList<>();

        for (ChatRoom room : chatRooms) {
            List<Message> messages = messageRepository.findByChatRoomOrderBySentAtAsc(room);
            responses.add(mapToChatRoomResponse(room, user, messages));
        }

        // Sort by lastMessageTime descending (rooms with recent messages first, nulls last)
        responses.sort((r1, r2) -> {
            if (r1.getLastMessageTime() == null && r2.getLastMessageTime() == null) {
                return 0;
            }
            if (r1.getLastMessageTime() == null) {
                return 1;
            }
            if (r2.getLastMessageTime() == null) {
                return -1;
            }
            return r2.getLastMessageTime().compareTo(r1.getLastMessageTime());
        });

        return responses;
    }

    private MessageResponse mapToMessageResponse(Message message, Long currentUserId) {
        User sender = message.getSender();

        UserSummary senderSummary = UserSummary.builder()
                .userId(sender.getId())
                .firstName(sender.getFirstName())
                .lastName(sender.getLastName())
                .profilePhotoUrl(sender.getProfilePhotoUrl())
                .city(sender.getPreferences() != null ? sender.getPreferences().getCity() : null)
                .build();

        return MessageResponse.builder()
                .messageId(message.getId())
                .content(message.getContent())
                .sentAt(message.getSentAt() != null ? message.getSentAt().toString() : null)
                .status(message.getStatus())
                .sender(senderSummary)
                .isOwnMessage(message.getSender().getId().equals(currentUserId))
                .build();
    }

    private ChatRoomResponse mapToChatRoomResponse(ChatRoom room, User currentUser, List<Message> messages) {
        // Determine other participant
        User otherParticipant = room.getParticipant1().getId().equals(currentUser.getId())
                ? room.getParticipant2()
                : room.getParticipant1();

        UserSummary otherParticipantSummary = UserSummary.builder()
                .userId(otherParticipant.getId())
                .firstName(otherParticipant.getFirstName())
                .lastName(otherParticipant.getLastName())
                .profilePhotoUrl(otherParticipant.getProfilePhotoUrl())
                .city(otherParticipant.getPreferences() != null ? otherParticipant.getPreferences().getCity() : null)
                .build();

        // Get last message if exists
        String lastMessage = null;
        String lastMessageTime = null;
        if (!messages.isEmpty()) {
            Message lastMsg = messages.get(messages.size() - 1);
            lastMessage = lastMsg.getContent();
            lastMessageTime = lastMsg.getSentAt() != null ? lastMsg.getSentAt().toString() : null;
        }

        // Count unread messages (messages from other user that are not READ)
        int unreadCount = (int) messages.stream()
                .filter(msg -> !msg.getSender().getId().equals(currentUser.getId()))
                .filter(msg -> msg.getStatus() != MessageStatus.READ)
                .count();

        return ChatRoomResponse.builder()
                .chatRoomId(room.getId())
                .otherParticipant(otherParticipantSummary)
                .lastMessage(lastMessage)
                .lastMessageTime(lastMessageTime)
                .unreadCount(unreadCount)
                .build();
    }
}
