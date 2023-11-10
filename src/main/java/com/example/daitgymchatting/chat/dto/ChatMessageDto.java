package com.example.daitgymchatting.chat.dto;

import com.example.daitgymchatting.chat.entity.ChatMessage;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ChatMessageDto {

    private Long chatMessageId;
    private String sender;
    private String message;
    private String redisRoomId;
    private int readCount;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;
    private Duration timeDifference;


    public ChatMessageDto(ChatMessage chatMessage) {
        this.chatMessageId = chatMessage.getId();
        this.redisRoomId = chatMessage.getRedisRoomId();
        this.sender = chatMessage.getSender();
        this.message = chatMessage.getMessage();
        this.readCount = chatMessage.getReadCount();
        this.createdAt = LocalDateTime.now();
    }


    public void setReadCount(int readCount) {
        this.readCount = readCount;
    }
}
