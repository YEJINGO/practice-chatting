package com.example.daitgymchatting.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Response<T> {

    private String status;
    private String message;
    private T data;

    public static Response<Void> success(String message){
        return new Response<>("Success", message, null);
    }

    public static <T> Response<T> success(String message, T data){
        return new Response<>("Success", message, data);
    }

    public static <T> Response<T> success(T data){
        return new Response<>("Success", null, data);
    }


    public static Response<Void> error(String status, String message){
        return new Response<>(status, message, null);
    }

    public static Response<Void> error(String message){
        return new Response<>("Error", message, null);
    }
}