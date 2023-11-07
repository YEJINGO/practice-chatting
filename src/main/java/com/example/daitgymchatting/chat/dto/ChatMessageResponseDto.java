package com.example.daitgymchatting.chat.dto;

import com.example.daitgymchatting.chat.entity.ChatRoom;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ChatMessageResponseDto {
    private Long id;
    private String roomName;
    private String sender;
    private String redisRoomId;
    private String receiver;
    private String message;


    public ChatMessageResponseDto(Long id, String roomName, String redisRoomId, String sender, String receiver, String message) {
        this.id = id;
        this.roomName = roomName;
        this.redisRoomId = redisRoomId;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;

    }
}