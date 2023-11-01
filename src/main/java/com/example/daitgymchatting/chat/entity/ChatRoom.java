package com.example.daitgymchatting.chat.entity;

import com.example.daitgymchatting.chat.dto.UserInfoDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
public class ChatRoom implements Serializable {

    private static final long serialVersionUID = 6494678977089006639L;

    @Id
    @Column(name = "CHATROOM_ID")
    private String id;

    private String name;

    private UserInfoDto sender;

    @Builder
    public ChatRoom(String name, UserInfoDto sender) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.sender = sender;
    }
}
