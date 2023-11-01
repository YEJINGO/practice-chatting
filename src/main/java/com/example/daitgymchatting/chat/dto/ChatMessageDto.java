package com.example.daitgymchatting.chat.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatMessageDto {

    private String chatRoomId;
    private MessageType type;
    private String sender;
    private String message;

    @Builder
    public ChatMessageDto(String chatRoomId, MessageType type, String sender, String message) {
        this.chatRoomId = chatRoomId;
        this.type = type;
        this.sender = sender;
        this.message = message;
    }
}
