package com.example.daitgymchatting.member.dto.response;

import com.example.daitgymchatting.config.auth.LoginMember;
import com.example.daitgymchatting.member.entity.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
public class LoginMemberResponse {

    private Long userId;

    private String email;

    private Role role;

    private String nickName;
    private String imageUrl;

    private String accessToken;

    private String refreshToken;

    @Builder
    public LoginMemberResponse(LoginMember loginMember, String nickName, String imageUrl, String accessToken, String refreshToken) {
        this.userId = loginMember.getMember().getId();
        this.nickName = nickName;
        this.imageUrl = imageUrl;
        this.email = loginMember.getMember().getEmail();
        this.role = loginMember.getMember().getRole();
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}