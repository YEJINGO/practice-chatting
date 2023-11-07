package com.example.daitgymchatting.chat.entity;

import com.example.daitgymchatting.member.entity.Member;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class UsersChattingRoom {

    @EmbeddedId
    private UsersChattingRoomPk usersChattingRoomPk;

    @MapsId("memberId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @MapsId("roomId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;


    @Embeddable
    @NoArgsConstructor(access = PROTECTED)
    @EqualsAndHashCode
    @Getter
    public static class UsersChattingRoomPk implements Serializable {
        private Long memberId;
        private Long roomId;

        public UsersChattingRoomPk(Long memberId, Long roomId) {
            this.memberId = memberId;
            this.roomId = roomId;
        }
    }

    public UsersChattingRoom(Member member, ChatRoom chatRoom) {
        this.usersChattingRoomPk  = new UsersChattingRoomPk(member.getId(),chatRoom.getId());
        this.member = member;
        this.chatRoom = chatRoom;
    }
}
