package com.example.daitgymchatting.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(unique = true, length = 100)
    private String email;

    private String password;

    private String nickName;

    private String imageUrl;

    @Enumerated(value = STRING)
    private Role role;

    @Builder
    public Member(Long id, String email, String password, String nickName, String imageUrl, Role role) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nickName = nickName;
        this.imageUrl = imageUrl;
        this.role = role;
    }

    @Builder
    public Member(String nickName) {
        this.nickName = nickName;
    }
}
