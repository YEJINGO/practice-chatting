package com.example.daitgymchatting.chat.config;

import com.example.daitgymchatting.exception.chatException.StompExceptionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSockConfig implements WebSocketMessageBrokerConfigurer {
    private final StompHandler stompHandler;
    private final StompExceptionHandler stompExceptionHandler;


    /**
     * 서버와 처음 연결해주는 부분
     * Endpoint 는 클라이언트가 서버로 연결하는 특정 경로 또는 주소를 의미
     * WebSocket Open
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .setErrorHandler(stompExceptionHandler)
                .addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    /**
     * 메시지 송수신을 처리하는 부분
     * 메시지 브로커는 서버와 클라이언트 간의 메시지 교환을 관리하고 중계하는 시스템
     * 클라이언트와 서버 간의 메시지 교환을 관리하고 구성할 수 있으며, 클라이언트는 "/sub" 주제를 구독하여 서버로부터 메시지를 수신하고, "/pub" 주제를 사용하여 서버로 메시지를 보낼 수 있게 된다.
     * 이것은 STOMP 프로토콜을 통해 구현된 실시간 메시징 시스템을 구성하는데 사용된다.
     * enableSimpleBroker : sub로 보내면 이곳을 한번 거쳐서 프론트에 데이터전달, sub로 보내면 이곳을 한번 거쳐서 프론트에 데이터전달,클라이언트는 이 주제를 구독하여 서버에서 전송되는 메시지를 수신할 수 있음
     * setApplicationDestinationPrefixes : pub 로 데이터를 받으면 이곳을 한번 거쳐서 UIR 만 MessageMapping 에 매핑된다. ex) pub/chat/message 라면 pub 을 제외하고 /chat/message 를 @MessageMaping 에 매핑한다.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub");
        registry.setApplicationDestinationPrefixes("/pub/chat");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler);
    }
}
