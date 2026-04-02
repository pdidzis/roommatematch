package com.roommatematch.controller;

import com.roommatematch.dto.response.ChatRoomResponse;
import com.roommatematch.dto.response.MessageResponse;
import com.roommatematch.exception.ResourceNotFoundException;
import com.roommatematch.model.entity.User;
import com.roommatematch.repository.UserRepository;
import com.roommatematch.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Real-time messaging endpoints")
public class ChatController {

    private final ChatService chatService;
    private final UserRepository userRepository;

    private Long getUserIdFromAuthentication(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return user.getId();
    }

    @GetMapping("/rooms")
    @Operation(summary = "Get all my chat rooms")
    @ApiResponse(responseCode = "200", description = "Chat rooms retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    public ResponseEntity<List<ChatRoomResponse>> getMyChatRooms(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<ChatRoomResponse> chatRooms = chatService.getMyChatRooms(userId);
        return ResponseEntity.ok(chatRooms);
    }

    @GetMapping("/rooms/{roomId}/messages")
    @Operation(summary = "Get chat history for a room")
    @ApiResponse(responseCode = "200", description = "Messages retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "403", description = "Not a participant in this chat room")
    @ApiResponse(responseCode = "404", description = "Chat room not found")
    public ResponseEntity<List<MessageResponse>> getChatHistory(
            @PathVariable Long roomId,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<MessageResponse> messages = chatService.getChatHistory(userId, roomId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/rooms/open/{targetUserId}")
    @Operation(summary = "Open or create chat room with a user")
    @ApiResponse(responseCode = "200", description = "Chat room opened/created successfully")
    @ApiResponse(responseCode = "400", description = "You can only chat with accepted matches")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<ChatRoomResponse> openChatRoom(
            @PathVariable Long targetUserId,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        ChatRoomResponse chatRoom = chatService.getOrCreateChatRoom(userId, targetUserId);
        return ResponseEntity.ok(chatRoom);
    }
}
