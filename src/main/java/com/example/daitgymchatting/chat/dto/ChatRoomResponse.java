package com.example.daitgymchatting.chat.dto;

import com.example.daitgymchatting.chat.entity.ChatRoom;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ChatRoomResponse {
    private Long id;
    private String roomName;
    private String sender;
    private String redisRoomId;
    private String receiver;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;

    public ChatRoomResponse(ChatRoom chatRoom) {
        this.id = chatRoom.getId();
        this.roomName = chatRoom.getRoomName();
        this.sender = chatRoom.getSender();
        this.redisRoomId = chatRoom.getRedisRoomId();
        this.receiver = chatRoom.getReceiver();
        this.createdAt = chatRoom.getCreatedAt();
    }

    public ChatRoomResponse(String redisRoomId) {
        this.redisRoomId = redisRoomId;
    }
}
