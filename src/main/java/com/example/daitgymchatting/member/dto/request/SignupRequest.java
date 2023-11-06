package com.example.daitgymchatting.member.dto.request;

import com.example.daitgymchatting.member.entity.Member;
import com.example.daitgymchatting.member.entity.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequest {
    private String email;
    private String nickName;
    private String password;
    private String imageUrl;

    private Role role;

    @Builder
    public SignupRequest(String email, String nickName, String password, String imageUrl, Role role) {
        this.email = email;
        this.nickName = nickName;
        this.password = password;
        this.imageUrl = imageUrl;
        this.role = role;
    }

    public Member toEntity(String password) {
        return Member.builder()
                .email(this.email)
                .nickName(this.nickName)
                .password(password)
                .imageUrl(imageUrl)
                .role(this.role)
                .build();
    }
}
