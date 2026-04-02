package com.roommatematch.controller;

import com.roommatematch.dto.request.SendMessageRequest;
import com.roommatematch.dto.response.MessageResponse;
import com.roommatematch.dto.response.TypingIndicator;
import com.roommatematch.exception.ResourceNotFoundException;
import com.roommatematch.model.entity.User;
import com.roommatematch.repository.UserRepository;
import com.roommatematch.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    @MessageMapping("/chat.send/{roomId}")
    public void sendMessage(
            @DestinationVariable Long roomId,
            @Payload SendMessageRequest request,
            Principal principal) {

        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        MessageResponse messageResponse = chatService.sendMessage(
                user.getId(),
                roomId,
                request.getContent()
        );

        messagingTemplate.convertAndSend("/topic/chat/" + roomId, messageResponse);
        log.info("WebSocket message sent to room: {}", roomId);
    }

    @MessageMapping("/chat.typing/{roomId}")
    public void typingIndicator(
            @DestinationVariable Long roomId,
            Principal principal) {

        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        TypingIndicator indicator = TypingIndicator.builder()
                .chatRoomId(roomId)
                .senderName(user.getFirstName())
                .typing(true)
                .build();

        messagingTemplate.convertAndSend("/topic/chat/" + roomId + "/typing", indicator);
    }
}
