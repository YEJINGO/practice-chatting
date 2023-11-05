package com.example.daitgymchatting.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    ALREADY_REGISTERED_EMAIL(HttpStatus.BAD_REQUEST, "이미 가입된 이메일입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레쉬 토큰이 만료되었습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 카테고리입니다."),
    WRONG_APPROACH(HttpStatus.BAD_REQUEST, "잘못된 접근입니다."),
    POST_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 게시물입니다."),
    ALARM_CONTENT_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 알람 글귀입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자 입니다"),
    NOT_FOUND_ROOM(HttpStatus.NOT_FOUND, "존재하지 않는 채팅방입니다."),
    ;

    private HttpStatus status;

    private String message;
    }