//package com.example.daitgymchatting.member.entity;
//
//import jakarta.persistence.Id;
//import lombok.Builder;
//import lombok.Getter;
//import org.springframework.data.redis.core.RedisHash;
//import org.springframework.data.redis.core.TimeToLive;
//import org.springframework.data.redis.core.index.Indexed;
//import readnextday.readnextdayproject.config.auth.LoginMember;
//
//import javax.persistence.Id;
//
//import static readnextday.readnextdayproject.config.jwt.JwtProperties.REFRESH_TOKEN_EXPIRE_TIME_FOR_REDIS;
//
//
//@RedisHash(value = "refresh")
//@Getter
//public class RefreshToken {
//
//    @Id
//    private Long id;
//
//    private LoginMember loginMember;
//
//    @TimeToLive
//    private Long expiration = REFRESH_TOKEN_EXPIRE_TIME_FOR_REDIS;
//
//    @Indexed
//    private String refreshToken;
//
//    @Builder
//    public RefreshToken(LoginMember loginMember, String refreshToken) {
//        this.loginMember = loginMember;
//        this.refreshToken = refreshToken;
//    }
//}