package com.roommatematch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponse {

    private Long chatRoomId;

    private UserSummary otherParticipant;

    private String lastMessage;

    private String lastMessageTime;

    private Integer unreadCount;

    private String chatType;

    private String listingTitle;

    private Long listingId;

    private String landlordName;
}
