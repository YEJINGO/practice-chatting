package com.example.daitgymchatting.chat.config;

import com.example.daitgymchatting.config.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class StompHandler implements ChannelInterceptor {

    private final JwtUtils jwtUtils;

    /**
     * Websocket 연결 시 요청 header 의 jwt token 유효성을 검증하는 코드를 추가한다. 유효하지 않은 JWT 토큰일 경우, websocket을 연결하지 않고 예외 처리 한다.
     * headerAccessor : Websocket 프로토콜에서 사용되는 헤더 정보를 추출하기 위해 stompHeaderAccessor 를 사용하여 메시지를 매핑한다.
     * presend() : 메시지가 실제로 채널에 전송되기전에 호출된다. 즉, publisher가 send 하기 전에 호출된다.
     */


    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor headerAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(headerAccessor.getCommand())) {
            String token = headerAccessor.getFirstNativeHeader("Authentication");
            String tokenStompHeader = jwtUtils.getTokenStompHeader(token);
            jwtUtils.validateToken(tokenStompHeader);

        }
        return message;
    }
}
