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
    private String slackId;
    private String password;
    private Role role;

    @Builder
    public SignupRequest(String email, String slackId, String password, Role role) {
        this.email = email;
        this.slackId = slackId;
        this.password = password;
        this.role = role;
    }

    public Member toEntity(String password) {
        return Member.builder()
                .email(this.email)
                .nickName(this.slackId)
                .password(password)
                .role(this.role)
                .build();
    }
}
