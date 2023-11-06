package com.example.daitgymchatting.chat.dto;

import com.example.daitgymchatting.chat.entity.ChatMessage;
import com.example.daitgymchatting.chat.entity.MessageType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ChatMessageDto {

    private MessageType messageType;
    private String sender;
    private String message;
    private String redisRoomId;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;
    private Duration timeDifference;

    @Builder
    public ChatMessageDto(ChatMessage chatMessage) {
        this.redisRoomId = chatMessage.getRedisRoomId();
        this.messageType = chatMessage.getMessageType();
        this.sender = chatMessage.getSender();
        this.message = chatMessage.getMessage();
        this.createdAt = LocalDateTime.now();
    }

    public void setTimeDifference(Duration timeDifference) {
        this.timeDifference = timeDifference;
    }
}