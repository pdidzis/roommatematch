package com.roommatematch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypingIndicator {

    private Long chatRoomId;

    private String senderName;

    private boolean typing;
}
