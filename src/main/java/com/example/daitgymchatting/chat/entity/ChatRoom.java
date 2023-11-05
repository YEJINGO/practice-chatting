package com.example.daitgymchatting.chat.entity;

import com.example.daitgymchatting.member.entity.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @OneToMany(mappedBy = "chatRoom")
    private List<ChatMessage> chatMessageList = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder
    public ChatRoom(Long id, String roomName, String sender, String redisRoomId, Member member, String receiver) {
        this.id = id;
        this.roomName = roomName;
        this.sender = sender;
        this.redisRoomId = redisRoomId;
        this.member = member;
        this.receiver = receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;

    }
}
