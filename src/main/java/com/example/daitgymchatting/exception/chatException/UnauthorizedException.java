package com.example.daitgymchatting.exception.chatException;

import lombok.Getter;

@Getter
public class UnauthorizedException extends RuntimeException {
    private final String errorType;
    private final String errorMessage;

    public UnauthorizedException(String errorType, String errorMessage) {
        super(errorMessage);
        this.errorType = errorType;
        this.errorMessage = errorMessage;
    }

    public static UnauthorizedException of(String errorType, String errorMessage) {
        return new UnauthorizedException(errorType, errorMessage);
    }
}
