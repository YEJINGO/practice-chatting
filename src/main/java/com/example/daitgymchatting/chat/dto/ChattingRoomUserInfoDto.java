package com.example.daitgymchatting.chat.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChattingRoomUserInfoDto {

    private Long chatRoomId;
    private Long userId;
    private String userName;

    @Builder
    public ChattingRoomUserInfoDto(Long chatRoomId,Long userId,String userName) {
        this.chatRoomId = chatRoomId;
        this.userId = userId;
        this.userName = userName;
    }
}
