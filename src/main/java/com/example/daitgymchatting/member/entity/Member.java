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
    @Column(name = "MEMBER_ID")
    private Long id;

    @Column(unique = true, length = 100)
    private String email;

    private String password;

    private String slackId;

    @Enumerated(value = STRING)
    private Role role;

//    @OneToMany(mappedBy = "member", orphanRemoval = true)
//    private List<Post> post = new ArrayList<>();

    @Builder
    public Member(Long id, String email, String password, String slackId, Role role) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.slackId = slackId;
        this.role = role;
    }
}
