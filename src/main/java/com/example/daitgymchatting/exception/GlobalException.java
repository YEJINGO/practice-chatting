package com.example.daitgymchatting.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GlobalException extends RuntimeException{

    private final ErrorCode errorCode;
    private final String message;

    public GlobalException(ErrorCode code){
        this.errorCode = code;
        this.message = null;
    }

    @Override
    public String getMessage() {
        if(this.message == null){
            return this.errorCode.getMessage();
        }

        return message;
    }
}
