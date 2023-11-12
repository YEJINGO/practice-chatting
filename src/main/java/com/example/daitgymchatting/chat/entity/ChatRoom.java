package com.example.daitgymchatting.chat.entity;

import com.example.daitgymchatting.chat.dto.ChatRoomDto;
import com.example.daitgymchatting.member.entity.Member;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
public class ChatRoom {

    @Id
    @Column(name = "chat_room_id")
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String roomName;
    private String sender;
    private String redisRoomId;
    private String receiver;
    private String imageUrl;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;


    @OneToMany(mappedBy = "chatRoom")
    private List<ChatMessage> chatMessageList = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder
    public ChatRoom(ChatRoomDto chatRoomDto, Member member) {
        this.roomName = chatRoomDto.getReceiver();
        this.sender = chatRoomDto.getSender();
        this.redisRoomId = chatRoomDto.getRedisRoomId();
        this.member = member;
        this.imageUrl = member.getImageUrl();
        this.receiver = chatRoomDto.getReceiver();
        this.createdAt = LocalDateTime.now();
    }
}
