package com.example.daitgymchatting.chat.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class ChatMessage {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private MessageType messageType;
    private String sender;
    private String message;
    private String redisRoomId;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;
    private int readCount = 2;


    @ManyToOne
    @JoinColumn(name = "roomId")
    private ChatRoom chatRoom;

    @Builder
    public ChatMessage(MessageType messageType, String sender, ChatRoom chatRoom, String message, String redisRoomId) {
        super();
        this.messageType = messageType;
        this.sender = sender;
        this.chatRoom = chatRoom;
        this.message = message;
        this.redisRoomId = redisRoomId;
        this.createdAt = LocalDateTime.now();
    }


    public int setReadCount() {
        return --readCount;
    }

}
