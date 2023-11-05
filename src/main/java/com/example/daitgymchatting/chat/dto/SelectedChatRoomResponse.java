package com.example.daitgymchatting.chat.dto;

import com.example.daitgymchatting.chat.entity.ChatRoom;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
public class SelectedChatRoomResponse {

    private Long id;
    private String roomName;
    private String redisRoomId;
    private String sender;
    private String receiver;
    private List<ChatMessageDto> messages;

    @Builder
    public SelectedChatRoomResponse(ChatRoom chatRoom, List<ChatMessageDto> messages) {
        this.id = chatRoom.getId();
        this.roomName = chatRoom.getRoomName();
        this.redisRoomId = chatRoom.getRedisRoomId();
        this.sender = chatRoom.getSender();
        this.receiver = chatRoom.getReceiver();
        this.messages = messages;
    }
}