package com.roommatematch.dto.response;

import com.roommatematch.model.enums.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private Long messageId;

    private String content;

    private String sentAt;

    private MessageStatus status;

    private UserSummary sender;

    private boolean isOwnMessage;
}
