package com.example.daitgymchatting.chat.entity;

import com.example.daitgymchatting.chat.dto.MessageType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private MessageType messageType; // 메시지 타입
    private String sender; // 메시지 보낸사람
    private String message; // 메시지
    private String roomId;



    public static ChatMessage createChatMessage(String roomId, String sender, String message,MessageType type) {
        return ChatMessage.builder()
                .roomId(roomId)
                .sender(sender)
                .message(message)
                .messageType(type)
                .build();
    }
}