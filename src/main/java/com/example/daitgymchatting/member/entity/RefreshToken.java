package com.example.daitgymchatting.member.entity;

import com.example.daitgymchatting.config.auth.LoginMember;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import static com.example.daitgymchatting.config.jwt.JwtProperties.REFRESH_TOKEN_EXPIRE_TIME_FOR_REDIS;
import static jakarta.persistence.GenerationType.IDENTITY;


@RedisHash(value = "refresh")
@Getter
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private LoginMember loginMember;

    @TimeToLive
    private Long expiration = REFRESH_TOKEN_EXPIRE_TIME_FOR_REDIS;

    @Indexed
    private String refreshToken;

    @Builder
    public RefreshToken(LoginMember loginMember, String refreshToken) {
        this.loginMember = loginMember;
        this.refreshToken = refreshToken;
    }
}