package com.example.daitgymchatting.config.jwt;

public interface JwtProperties {
    long ACCESS_TOKEN_EXPIRATION_TIME = 24 * 60 * 60 * 1000L; // 1일
    long REFRESH_TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L; // 7일
    long REFRESH_TOKEN_EXPIRE_TIME_FOR_REDIS = REFRESH_TOKEN_EXPIRE_TIME / 1000; // 7일
    String TOKEN_PREFIX = "Bearer ";
    String HEADER_STRING = "Authorization";
}
